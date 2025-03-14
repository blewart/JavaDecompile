import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;

public class JarProcessor {

    // 定义存储目录
    private static final String UPLOAD_DIR = "/home/bs/test/tools/java_decompile/";
    private static final String DECOMPILER_JAR = "/home/bs/test/tools/java_decompile/java-decompiler.jar";

    public static String processJar(File jarFile) throws IOException, InterruptedException {
        // 1. 检查文件是否为 JAR
        if (!jarFile.getName().endsWith(".jar")) {
            return "上传的文件不是 JAR 包";
        }

        // 2. 生成目录名称：文件名 + 日期
        String fileNameWithoutExt = jarFile.getName().substring(0, jarFile.getName().lastIndexOf(".jar"));
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File jarDir = new File(UPLOAD_DIR + fileNameWithoutExt + "_" + timeStamp);
        if (!jarDir.mkdirs()) {
            return "无法创建存储目录";
        }

        // 3. 存储文件
        File targetFile = new File(jarDir, jarFile.getName());
        Files.copy(jarFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        // 4. 执行反编译
        String decompileCmd = String.format("java -cp %s org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler %s %s",
                DECOMPILER_JAR, targetFile.getAbsolutePath(), jarDir.getAbsolutePath());
        executeCommand(decompileCmd);

        // 5. 进入 JAR 目录解压
        File codeDir = new File(jarDir, "code");
        if (!codeDir.exists() && !codeDir.mkdirs()) {
            return "反编译失败，code 目录无法创建";
        }

        // 6. 解压 JAR 包
        String extractCmd = String.format("jar -xvf %s", targetFile.getAbsolutePath());
        executeCommand(extractCmd, codeDir);

        return "处理完成，存储目录: " + jarDir.getAbsolutePath();
    }

    private static void executeCommand(String command) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec(command);
        process.waitFor();
    }

    private static void executeCommand(String command, File dir) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder(command.split(" "));
        builder.directory(dir);
        Process process = builder.start();
        process.waitFor();
    }

    public static void main(String[] args) {
        try {
            File jarFile = new File("/path/to/uploaded.jar");
            System.out.println(processJar(jarFile));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}