import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class JavaDecompiler {
    public static void decompilerJar(String string) {
        // 校验 JAR 文件是否存在
        File jarFile = new File(string);
        if (!jarFile.exists() || !jarFile.isFile()) {
            System.err.println("指定的 JAR 文件不存在：" + string);
            System.exit(1);
        }

        // 其他路径定义
        String decompilerJarPath = "D:\\java_parse_tool\\java-decompiler.jar";
        String baseOutputDir = "D:\\java_parse_tool\\";

        String jarFileName = jarFile.getName();
        String jarNameWithoutExt = jarFileName.substring(0, jarFileName.lastIndexOf('.'));
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String outputDir = baseOutputDir + timestamp + "-" + jarNameWithoutExt;

        // 创建输出目录
        File outDir = new File(outputDir);
        if (!outDir.exists() && !outDir.mkdirs()) {
            System.err.println("无法创建输出目录：" + outputDir);
            System.exit(1);
        }

        // 反编译命令
        List<String> decompileCommand = List.of(
                "java",
                "-cp",
                decompilerJarPath,
                "org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler",
                string,
                outputDir
        );

        try {
            ProcessBuilder pb = new ProcessBuilder(decompileCommand);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.err.println("反编译过程返回错误码：" + exitCode);
                System.exit(1);
            }

            // 解压 JAR
            String sourceFolder = outputDir + "\\source";
            File sourceDir = new File(sourceFolder);
            if (!sourceDir.exists() && !sourceDir.mkdirs()) {
                System.err.println("无法创建 source 目录：" + sourceFolder);
                System.exit(1);
            }

            List<String> jarExtractCommand = List.of(
                    "jar",
                    "-xvf",
                    "..\\" + jarFileName
            );

            ProcessBuilder pbJar = new ProcessBuilder(jarExtractCommand);
            pbJar.directory(sourceDir);
            pbJar.redirectErrorStream(true);
            Process jarProcess = pbJar.start();

            try (BufferedReader jarReader = new BufferedReader(new InputStreamReader(jarProcess.getInputStream()))) {
                String line;
                while ((line = jarReader.readLine()) != null) {
                    System.out.println(line);
                }
            }

            int jarExitCode = jarProcess.waitFor();
            if (jarExitCode != 0) {
                System.err.println("解压 jar 过程返回错误码：" + jarExitCode);
            } else {
                System.out.println("反编译与解压完成，结果目录：" + outputDir);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}

