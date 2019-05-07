package com.leyou.upload.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class UploadService {

    private static final List<String> ALLOW_TYPES = Arrays.asList("image/jpeg", "image/png", "image/bmp","image/jpg");

    public String upLoadImage(MultipartFile file) {
        try {
            //校验文件类型  因为可以改后缀名，所以还要校验内容
            String contentType = file.getContentType();
            if (!ALLOW_TYPES.contains(contentType)) {
                throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
            }
            //校验文件内容  严谨点的还需要校验文件的长宽高，判断是不是真的图片
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
            }

            //目标路径
            //this.getClass().getClassLoader().getResource("").getFile()
            //File dest = new File("/dlion/upload/", file.getOriginalFilename());//获取项目路径
            File dest = new File("C:/dlion/upload",file.getOriginalFilename());
            //保存文件到本地
            file.transferTo(dest);
            //返回路径
            return "http://image.leyou.com/" + file.getOriginalFilename();
        } catch (IOException e) {
            //上传失败
            log.error("上传文件失败", e);
            throw new LyException(ExceptionEnum.UPLOAD_FILE_ERROR);
        }
    }
}
