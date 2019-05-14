/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pdfbox.tools;

import org.apache.pdfbox.contentstream.PDFStreamEngine;
import org.apache.pdfbox.contentstream.operator.DrawObject;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.contentstream.operator.text.ShowText;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdfwriter.ContentStreamWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.util.Charsets;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Extracts the images from a PDF file.
 *
 * @author Ben Litchfield
 */
public final class EditPdf {


    /**
     * Entry point for the application.
     *
     * @param args The command-line arguments.
     * @throws IOException if there is an error reading the file or extracting the images.
     */
    public static void main(String[] args) throws IOException {
        EditPdf editPdf = new EditPdf();
        for (String template : editPdfBoTemplate.keySet()) {
            EditPdfBO editPdfBO = editPdfBoTemplate.get(template);
            BufferedImage bufferedImage = ImageIO.read(new File(editPdfBO.getLogo()));
            simhashTemplate = editPdf.simHashImage(bufferedImage);
            editPdf.extract(editPdfBO);
        }
    }


    private EditPdfBO editPdfBO;
    private List<Object> tokens = new ArrayList<>();
    public static final String _51JOB = "51job";
    public static final String ZHILIAN = "zhilian";
    public static final String XINZHIYE = "xinzhiye";
    private static Map<String, EditPdfBO> editPdfBoTemplate = new HashMap<>();

    static {
        EditPdfBO _51job = new EditPdfBO("/Users/harry/install-workspace/简历/模板/51job.pdf",
                "/Users/harry/install-workspace/简历/模板/51job-1.png",
                "来自前程无忧简历",
                "/Users/harry/install-workspace/简历/模板/51job-dest.pdf", false);

        EditPdfBO zhilian = new EditPdfBO("/Users/harry/install-workspace/简历/模板/zhilian.pdf",
                "/Users/harry/install-workspace/简历/模板/zhilain-1.jpg",
                "",
                "/Users/harry/install-workspace/简历/模板/zhilian-desc.pdf", true);


        EditPdfBO xinzhiye = new EditPdfBO("/Users/harry/install-workspace/简历/模板/xinzhiye.pdf",
                "/Users/harry/install-workspace/简历/模板/xinzhiye-1.png",
                "4E0F261539F4547919F6383F52DB20A951E00CA70CA737E8534163772E2738482BB62DF547AE2EC020A951E04D680CA70CA7005A005A005A00110051004600560056001100520055004A001100460051001200620003",
                "/Users/harry/install-workspace/简历/模板/xinzhiye-dest.pdf", true);


        editPdfBoTemplate.put(_51JOB, _51job);
        editPdfBoTemplate.put(ZHILIAN, zhilian);
        editPdfBoTemplate.put(XINZHIYE, xinzhiye);
    }

    private int hammingDistance(BigInteger simHash, BigInteger otherSimHash) {
        BigInteger x = simHash.xor(otherSimHash);
        int distance = 0;

        // 统计x中二进制位数为1的个数
        // 我们想想，一个二进制数减去1，那么，从最后那个1（包括那个1）后面的数字全都反了，对吧，然后，n&(n-1)就相当于把后面的数字清0，
        // 我们看n能做多少次这样的操作就OK了。

        while (x.signum() != 0) {
            distance += 1;
            x = x.and(x.subtract(new BigInteger("1")));
        }
        return distance;
    }

    private BufferedImage resizeImage(BufferedImage srcImg, int width, int height) {
        BufferedImage buffImg = null;
        buffImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        buffImg.getGraphics().drawImage(srcImg.getScaledInstance(width, height, Image.SCALE_SMOOTH), 0, 0, null);
        return buffImg;
    }

    private BigInteger simHashImage(BufferedImage srcImage) throws IOException {
        srcImage = resizeImage(srcImage, 64, 64);
        // Gray = R*0.299 + G*0.587 + B*0.114
        int colorSum = 0;
        int[] pixed = new int[64];
        int index = 0;
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                int rgb = srcImage.getRGB(x, y);
                int r = (rgb & 0xff0000) >> 16;
                int g = (rgb & 0xff00) >> 8;
                int b = (rgb & 0xff);
                // +500保证四舍五入
                pixed[index] = (r * 299 + g * 587 + b * 114 + 500) / 1000;
                colorSum += pixed[index];
                index++;
            }
        }
        int avg = colorSum / 64;

        BigInteger fingerprint = new BigInteger("0");
        for (int i = 0; i < 64; i++) {
            if (pixed[i] > avg) {
                fingerprint = fingerprint.add(new BigInteger("1").shiftLeft(i));
            }
        }
        return fingerprint;
    }

    private static BigInteger simhashTemplate;

    private static void writeTokensToStream(PDStream newContents, List<Object> newTokens) throws IOException {
        try (OutputStream out = newContents.createOutputStream(COSName.FLATE_DECODE)) {
            ContentStreamWriter writer = new ContentStreamWriter(out);
            writer.writeTokens(newTokens);
        }
    }

    private void extract(EditPdfBO editPdf) throws IOException {
        this.editPdfBO = editPdf;

        try (PDDocument document = PDDocument.load(new File(editPdf.getSrcFilePath()))) {

            PDFStreamEditorEngine extractor = new PDFStreamEditorEngine();
            for (PDPage page : document.getPages()) {
                tokens.clear();
                extractor.processPage(page);
                PDStream newContents = new PDStream(document);
                writeTokensToStream(newContents, tokens);
                page.setContents(newContents);
            }
            document.save(editPdfBO.getDescFilePath());
        }
    }

    private class PDFStreamEditorEngine extends PDFStreamEngine {

        @Override
        protected void processOperator(Operator operator, List<COSBase> operands) throws IOException {
            super.processOperator(operator, operands);
            if (operator.getName().equals(new ShowText().getName())) {
                COSString str = (COSString) operands.get(0);
                String content = EditPdf.this.editPdfBO.isHex() ? str.toHexString() : new String(str.getBytes(), Charsets.UTF_16BE);
                if (!EditPdf.this.editPdfBO.getTextMark().equals(content)) {
                    tokens.addAll(operands);
                    tokens.add(operator);
                }
            } else if (operator.getName().equals(new DrawObject().getName())) {
                COSName objectName = (COSName) operands.get(0);
                //resources
                PDImageXObject pdImage = (PDImageXObject) this.getResources().getXObject(objectName);
                BufferedImage image = pdImage.getImage();
                BigInteger simhash = simHashImage(image);
                if (hammingDistance(simhash, simhashTemplate) > 3) {
                    tokens.addAll(operands);
                    tokens.add(operator);
                }
            } else {
                tokens.addAll(operands);
                tokens.add(operator);
            }
        }
    }
}
