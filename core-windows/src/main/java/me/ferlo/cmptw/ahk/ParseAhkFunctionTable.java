package me.ferlo.cmptw.ahk;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

class ParseAhkFunctionTable {

    private ParseAhkFunctionTable() {
    }

    public static void main(String[] args) throws IOException {
        Pattern regex = Pattern.compile("<tr(?:\\s+id=\".*?\")?>\\s*<td(?:\\s+class=\".*?\")?>(?:<a\\s+href=\"(?<url>.*?)\">)?(?<var>(?:\\s|\\S)*?)(?:</a>)?</td>\\s*<td>(?<desc>(?:\\s|\\S)*?)</td>\\s*</tr>");

        final var text = Files.readString(Path.of("ahk_functions.htm"), StandardCharsets.UTF_8);
        final var matcher = regex.matcher(text);

        final Set<Match> directives = new LinkedHashSet<>();
        final Set<Match> functions = new LinkedHashSet<>();
        final Set<Match> commands = new LinkedHashSet<>();
        while (matcher.find()) {
            String name = matcher.group("var").strip()
                    .replace("\n", "")
                    .replace("\r", "")
                    .replace("<br>", "")
                    .strip();

            final Set<Match> toAdd;
            if(name.startsWith("#"))
                toAdd = directives;
            else if(name.endsWith("()")) {
                toAdd = functions;
                name = name.substring(0, name.length() - "()".length());
            } else
                toAdd = commands;

            toAdd.add(new Match(
                    name,
                    matcher.group("url") != null ? matcher.group("url").strip()
                            .replace("\n", "")
                            .replace("\r", "")
                            .replace("<br>", "")
                            .strip() : "",
                    matcher.group("desc")
                            .strip()
                            .replace("\\", "\\\\")
                            .replace("\"", "\\\"")
                            .replaceAll("\s*\n\s+", "") // Then fix pre tags
                            .replace("\n", "<br>")
                            .replace("\r", "")
                            .replace("<a href=\\\"", "<a href=\\\"\" + website + \"")
                            .strip()
            ));
        }

        for(Map.Entry<Set<Match>, String> input : List.of(
                new AbstractMap.SimpleEntry<>(functions, "Functions"),
                new AbstractMap.SimpleEntry<>(commands, "Commands"),
                new AbstractMap.SimpleEntry<>(directives, "Directives"))) {
            System.out.println("// " + input.getValue());
            for (Match match : input.getKey()) {
                System.out.print("code.addCompletion(variableCompletion(code, \"");
                System.out.print(match.name());
                System.out.print("\", \"\", \"");
                System.out.print(match.desc());
                System.out.print("<br><p>See <a href=\\\"\" + website + \"" + match.url() + "\\\">" + match.name() + "</a></p>");
                System.out.println("\"));");
            }
        }
    }

    record Match(String name, String url, String desc) {
    }
}
