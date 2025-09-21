package com.boazhousing.qrcodegen.model;

public class QrGenerationResult {
    private String reference;
    private String pdfUrl;
    private String fileName;
    private String filePath;
    private long fileSize;

    public QrGenerationResult() {}

    public QrGenerationResult(String reference, String pdfUrl, String fileName, String filePath, long fileSize) {
        this.reference = reference;
        this.pdfUrl = pdfUrl;
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileSize = fileSize;
    }

    // Getters and Setters
    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getPdfUrl() {
        return pdfUrl;
    }

    public void setPdfUrl(String pdfUrl) {
        this.pdfUrl = pdfUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
}