package com.wildma.idcardcamera.global;

import com.wildma.idcardcamera.utils.FileUtils;

import java.io.File;

public class Constant {
    public static final String APP_NAME = "LibraryFojadores";
    public static final String BASE_DIR = APP_NAME + File.separator;
    public static final String DIR_ROOT = FileUtils.getRootPath() + File.separator + Constant.BASE_DIR;
}