using System;
using System.Diagnostics;
using System.IO;
using System.Text;

internal static class SetupLauncher
{
    private const string AndroidStudioJavaHome = @"C:\Program Files\Android\Android Studio\jbr";

    private static bool dryRun;
    private static bool noPause;

    private static int Main(string[] args)
    {
        Console.OutputEncoding = Encoding.UTF8;
        dryRun = HasArgument(args, "--dry-run");
        noPause = HasArgument(args, "--no-pause");

        try
        {
            string setupDirectory = AppDomain.CurrentDomain.BaseDirectory.TrimEnd(
                Path.DirectorySeparatorChar,
                Path.AltDirectorySeparatorChar);
            DirectoryInfo setupInfo = new DirectoryInfo(setupDirectory);
            string projectRoot = setupInfo.Parent == null ? null : setupInfo.Parent.FullName;

            if (string.IsNullOrWhiteSpace(projectRoot) ||
                !Directory.Exists(Path.Combine(projectRoot, "backend")) ||
                !Directory.Exists(Path.Combine(projectRoot, "mobile")))
            {
                return FinishWithError(
                    "A pasta setup deve permanecer dentro da raiz do projeto Mercado.");
            }

            string action = Path.GetFileNameWithoutExtension(
                Process.GetCurrentProcess().MainModule.FileName);

            if (action.Equals("Inicializar Backend", StringComparison.OrdinalIgnoreCase))
            {
                return StartBackend(projectRoot);
            }

            if (action.Equals("Inicializar Frontend", StringComparison.OrdinalIgnoreCase))
            {
                return StartFrontend(projectRoot);
            }

            if (action.Equals("Gerar APK", StringComparison.OrdinalIgnoreCase))
            {
                return GenerateApk(projectRoot);
            }

            if (action.Equals("Executar Testes Frontend", StringComparison.OrdinalIgnoreCase))
            {
                return RunFrontendTests(projectRoot);
            }

            if (action.Equals("Executar Testes Backend", StringComparison.OrdinalIgnoreCase))
            {
                return RunBackendTests(projectRoot);
            }

            return FinishWithError("Ação desconhecida para o executável: " + action);
        }
        catch (Exception exception)
        {
            return FinishWithError("Falha inesperada: " + exception.Message);
        }
    }

    private static int StartBackend(string projectRoot)
    {
        PrintHeader("Inicializar backend");
        int exitCode = RunCommand(
            BuildMavenCommand("spring-boot:run"),
            Path.Combine(projectRoot, "backend"),
            null);
        return Finish(
            exitCode,
            exitCode == 0 ? "Backend finalizado." : "Falha ao executar o backend.");
    }

    private static int StartFrontend(string projectRoot)
    {
        PrintHeader("Inicializar frontend");
        int exitCode = RunCommand(
            "npm start",
            Path.Combine(projectRoot, "mobile"),
            null);
        return Finish(
            exitCode,
            exitCode == 0 ? "Frontend finalizado." : "Falha ao executar o frontend.");
    }

    private static int RunFrontendTests(string projectRoot)
    {
        PrintHeader("Executar testes do frontend");
        int exitCode = RunCommand(
            "npm run test:ci",
            Path.Combine(projectRoot, "mobile"),
            null);
        return Finish(
            exitCode,
            exitCode == 0
                ? "Testes do frontend concluídos."
                : "Os testes do frontend falharam.");
    }

    private static int RunBackendTests(string projectRoot)
    {
        PrintHeader("Executar testes do backend");
        int exitCode = RunCommand(
            BuildMavenCommand("test"),
            Path.Combine(projectRoot, "backend"),
            null);
        return Finish(
            exitCode,
            exitCode == 0
                ? "Testes do backend concluídos."
                : "Os testes do backend falharam.");
    }

    private static int GenerateApk(string projectRoot)
    {
        PrintHeader("Gerar APK Android");

        string mobileDirectory = Path.Combine(projectRoot, "mobile");
        string androidDirectory = Path.Combine(mobileDirectory, "android");
        string javaHome = FindAndroidJavaHome();

        if (javaHome == null)
        {
            return FinishWithError(
                "Java 21 ou superior não encontrado. Instale o Android Studio ou um JDK 21+." );
        }

        Console.WriteLine("Java do build Android: " + javaHome);
        Console.WriteLine();

        int exitCode = RunCommand("npm run build", mobileDirectory, null);
        if (exitCode != 0)
        {
            return Finish(exitCode, "Falha ao gerar os arquivos web.");
        }

        exitCode = RunCommand("npx cap sync android", mobileDirectory, null);
        if (exitCode != 0)
        {
            return Finish(exitCode, "Falha ao sincronizar o projeto Android.");
        }

        exitCode = RunCommand("gradlew.bat assembleDebug", androidDirectory, javaHome);
        if (exitCode != 0)
        {
            return Finish(exitCode, "Falha ao compilar o APK.");
        }

        string apkPath = Path.Combine(
            androidDirectory,
            "app",
            "build",
            "outputs",
            "apk",
            "debug",
            "app-debug.apk");

        if (!dryRun && !File.Exists(apkPath))
        {
            return FinishWithError("O Gradle terminou, mas o APK não foi encontrado em: " + apkPath);
        }

        Console.ForegroundColor = ConsoleColor.Green;
        Console.WriteLine();
        Console.WriteLine("APK gerado com sucesso:");
        Console.WriteLine(apkPath);
        Console.ResetColor();

        return Finish(0, null);
    }

    private static string FindAndroidJavaHome()
    {
        if (File.Exists(Path.Combine(AndroidStudioJavaHome, "bin", "java.exe")))
        {
            return AndroidStudioJavaHome;
        }

        string programFiles = Environment.GetFolderPath(Environment.SpecialFolder.ProgramFiles);
        string javaDirectory = Path.Combine(programFiles, "Java");
        string[] preferredJdks = { "jdk-21", "jdk-22", "jdk-23", "jdk-24", "jdk-25" };

        foreach (string preferredJdk in preferredJdks)
        {
            string candidate = Path.Combine(javaDirectory, preferredJdk);
            if (File.Exists(Path.Combine(candidate, "bin", "java.exe")))
            {
                return candidate;
            }
        }

        return null;
    }

    private static string BuildMavenCommand(string goal)
    {
        string userProfile = Environment.GetEnvironmentVariable("USERPROFILE");
        if (string.IsNullOrWhiteSpace(userProfile))
        {
            return "mvn.cmd " + goal;
        }

        string localRepository = Path.Combine(userProfile, ".m2", "repository");
        return "mvn.cmd -Dmaven.repo.local=\"" + localRepository + "\" " + goal;
    }

    private static int RunCommand(string command, string workingDirectory, string javaHome)
    {
        Console.ForegroundColor = ConsoleColor.Cyan;
        Console.WriteLine("> " + command);
        Console.ResetColor();
        Console.WriteLine("Pasta: " + workingDirectory);
        Console.WriteLine();

        if (dryRun)
        {
            return 0;
        }

        string commandProcessor = Environment.GetEnvironmentVariable("ComSpec");
        if (string.IsNullOrWhiteSpace(commandProcessor))
        {
            commandProcessor = "cmd.exe";
        }

        ProcessStartInfo startInfo = new ProcessStartInfo
        {
            FileName = commandProcessor,
            Arguments = "/d /c \"" + command + "\"",
            WorkingDirectory = workingDirectory,
            UseShellExecute = false
        };

        if (!string.IsNullOrWhiteSpace(javaHome))
        {
            startInfo.EnvironmentVariables["JAVA_HOME"] = javaHome;
        }

        using (Process process = Process.Start(startInfo))
        {
            process.WaitForExit();
            return process.ExitCode;
        }
    }

    private static int Finish(int exitCode, string message)
    {
        if (!string.IsNullOrWhiteSpace(message))
        {
            Console.WriteLine();
            Console.ForegroundColor = exitCode == 0 ? ConsoleColor.Green : ConsoleColor.Red;
            Console.WriteLine(message);
            Console.ResetColor();
        }

        if (exitCode != 0)
        {
            Console.ForegroundColor = ConsoleColor.Red;
            Console.WriteLine("Código de saída: " + exitCode);
            Console.ResetColor();
        }

        PauseIfNeeded();
        return exitCode;
    }

    private static int FinishWithError(string message)
    {
        Console.ForegroundColor = ConsoleColor.Red;
        Console.WriteLine(message);
        Console.ResetColor();
        PauseIfNeeded();
        return 1;
    }

    private static void PrintHeader(string title)
    {
        Console.Title = title + " - Mercado";
        Console.WriteLine("============================================================");
        Console.WriteLine(title);
        Console.WriteLine("============================================================");
        Console.WriteLine();
    }

    private static void PauseIfNeeded()
    {
        if (dryRun || noPause)
        {
            return;
        }

        Console.WriteLine();
        Console.WriteLine("Pressione ENTER para fechar esta janela.");
        Console.ReadLine();
    }

    private static bool HasArgument(string[] args, string expected)
    {
        foreach (string argument in args)
        {
            if (argument.Equals(expected, StringComparison.OrdinalIgnoreCase))
            {
                return true;
            }
        }

        return false;
    }
}
