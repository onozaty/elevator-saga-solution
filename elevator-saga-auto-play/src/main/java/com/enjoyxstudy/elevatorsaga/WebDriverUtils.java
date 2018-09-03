package com.enjoyxstudy.elevatorsaga;

public class WebDriverUtils {

    public static String defualtDriverPath() {

        String osName = System.getProperty("os.name").toLowerCase();

        if (osName.startsWith("windows")) {
            return "driver/chromedriver-win32.exe";
        }

        if (osName.startsWith("mac")) {
            return "driver/chromedriver-mac64";
        }

	if (osName.startsWith("linux")) {
		return "driver/chromedriver-linux64";
	}

        throw new UnsupportedOperationException("対応していないOSです。 os.name:" + osName);
    }
}
