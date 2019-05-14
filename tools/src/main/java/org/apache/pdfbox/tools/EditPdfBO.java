package org.apache.pdfbox.tools;

/**
 * @author: zh_harry@163.com
 * @date: 2019-04-12 18:43
 * @description:
 */
public class EditPdfBO {
    public EditPdfBO(String srcFilePath,
                     String logo,
                     String textMark,
                     String descFilePath,
                     boolean hex) {
        this.srcFilePath = srcFilePath;
        this.logo = logo;
        this.textMark = textMark;
        this.descFilePath = descFilePath;
        this.hex = hex;
    }

    private String srcFilePath;
    private String logo;
    private String textMark;
    private String descFilePath;
    private boolean hex;

    public String getSrcFilePath() {
        return srcFilePath;
    }

    public void setSrcFilePath(String srcFilePath) {
        this.srcFilePath = srcFilePath;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getTextMark() {
        return textMark;
    }

    public void setTextMark(String textMark) {
        this.textMark = textMark;
    }

    public String getDescFilePath() {
        return descFilePath;
    }

    public void setDescFilePath(String descFilePath) {
        this.descFilePath = descFilePath;
    }

    public boolean isHex() {
        return hex;
    }

    public void setHex(boolean hex) {
        this.hex = hex;
    }
}
