package com.supershade.shizuku;

import android.content.Context;
import androidx.annotation.Keep;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

@Keep
public class ShadeCommanderService extends IShadeCommander.Stub {

    @Keep
    public ShadeCommanderService() {
    }

    @Keep
    public ShadeCommanderService(Context context) {
    }

    @Override
    public boolean exec(String[] cmd) {
        try {
            Process process = new ProcessBuilder(cmd).redirectErrorStream(true).start();
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                while (reader.readLine() != null) {
                    // Drain output so pipe buffer never blocks the child process
                }
                return process.waitFor(5, TimeUnit.SECONDS) && process.exitValue() == 0;
            } finally {
                process.getInputStream().close();
                process.getOutputStream().close();
                process.destroy();
            }
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String execForOutput(String[] cmd) {
        try {
            Process process = new ProcessBuilder(cmd).redirectErrorStream(true).start();
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    if (sb.length() > 0) sb.append("\n");
                    sb.append(line);
                }
                process.waitFor(5, TimeUnit.SECONDS);
                return sb.toString().trim();
            } finally {
                process.getInputStream().close();
                process.getOutputStream().close();
                process.destroy();
            }
        } catch (Exception e) {
            return "";
        }
    }

    public void destroy() {
        System.exit(0);
    }
}
