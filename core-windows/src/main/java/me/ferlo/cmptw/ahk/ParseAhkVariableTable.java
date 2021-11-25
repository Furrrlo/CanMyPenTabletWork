package me.ferlo.cmptw.ahk;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

class ParseAhkVariableTable {

    private ParseAhkVariableTable() {
    }

    public static void main(String[] args) throws IOException {
        Pattern regex = Pattern.compile("<tr(?:\\s+id=\".*?\")?>\\s*<td>(?<var>(?:\\s|\\S)*?)</td>\\s*<td>(?<desc>(?:\\s|\\S)*?)</td>\\s*</tr>");

        for(Map.Entry<String, String> input : List.of(
                new AbstractMap.SimpleEntry<>("ahk_variables.htm", "Variables"),
                new AbstractMap.SimpleEntry<>("ahk_fileloop_variables.htm", "File Loop Variables"),
                new AbstractMap.SimpleEntry<>("ahk_regloop_variables.htm", "Registry Loop Variables"))) {
            final var text = Files.readString(Path.of(input.getKey()), StandardCharsets.UTF_8);
            final var matcher = regex.matcher(text);

            System.out.println("// " + input.getValue());
            while (matcher.find()) {
//                System.out.print("\"");
//                System.out.print(matcher.group("var").strip()
//                        .replace("\n", "")
//                        .replace("\r", "")
//                        .replace("<br>", "")
//                        .replaceAll("<span class=\"ver\">.*?</span>", "")
//                        .strip());
//                System.out.print("\" | ");
                System.out.print("code.addCompletion(functionCompletion(code, \"");
                System.out.print(matcher.group("var").strip()
                        .replace("\n", "")
                        .replace("\r", "")
                        .replace("<br>", "")
                        .replaceAll("<span class=\"ver\">.*?</span>", "")
                        .strip());
                System.out.print("\", \"\", \"");
                System.out.print(matcher.group("desc")
                        .strip()
                        .replace("\\", "\\\\")
                        .replace("\"", "\\\"")
                        .replaceAll("\s*\n\s+", "") // Then fix pre tags
                        .replace("\n", "<br>")
                        .replace("\r", "")
                        .replace("<a href=\\\"", "<a href=\\\"\" + website + \"")
                        .strip());
                System.out.println("\"));");
            }
        }
    }
}
