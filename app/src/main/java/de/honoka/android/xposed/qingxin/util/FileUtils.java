package de.honoka.android.xposed.qingxin.util;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import lombok.SneakyThrows;

public class FileUtils {

    @SneakyThrows
    public static String streamToString(InputStream is) {
        byte[] bytes = new byte[is.available()];
        is.read(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * 获取当前运行环境的classpath
     */
    public static String getClasspath() {
        try {
            return new File(Thread.currentThread().getContextClassLoader()
                    .getResource("").toURI()).getAbsolutePath();
        } catch(Exception e) {
            return new File("").getAbsolutePath();
        }
    }

    @SneakyThrows
    public static void checkDirs(File... dirs) {
        for(File dir : dirs) {
            if(!dir.exists()) dir.mkdirs();
        }
    }

    /**
     * 检查必要的文件是否存在，不存在则创建
     */
    @SneakyThrows
    public static void checkFiles(File... files) {
        for(File f : files) {
            if(!f.exists()) {    //文件不存在
                //检查文件所在的的目录是否存在，不存在先创建多级目录
                File path = f.getParentFile();
                if(!path.exists()) path.mkdirs();
                f.createNewFile();
            }
        }
    }
}
