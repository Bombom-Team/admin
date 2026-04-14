package me.bombom.api.v1.file.dto;

public record UploadFileResponse(String url) {

    public static UploadFileResponse from(StoredFile storedFile) {
        return new UploadFileResponse(storedFile.fileUrl());
    }
}
