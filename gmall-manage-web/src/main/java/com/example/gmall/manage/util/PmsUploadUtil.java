package com.example.gmall.manage.util;

import org.apache.commons.lang3.StringUtils;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.web.multipart.MultipartFile;

public class PmsUploadUtil {
    public String uploadImage(MultipartFile multipartFile) {
        // 获取文件所在路径
        String file = PmsUploadUtil.class.getResource("/fdfs_client.conf").getFile();
        try {
            ClientGlobal.init(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        TrackerClient trackerClient = new TrackerClient();
        TrackerServer trackerServer = null;
        try {
            trackerServer = trackerClient.getTrackerServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
        StorageClient storageClient = new StorageClient(trackerServer, null);
        String s = "";
        try {
            String originalFilename = multipartFile.getOriginalFilename();
            // String extName = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
            String extName = StringUtils.substringAfterLast(originalFilename, ".");
            String[] uploadFile = storageClient.upload_file(multipartFile.getBytes(), extName, null);
            for (int i = 0; i < uploadFile.length; i++) {
                s += "/" + uploadFile[i];

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return s;

    }
}
