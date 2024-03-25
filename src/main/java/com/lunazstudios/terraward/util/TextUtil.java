package com.lunazstudios.terraward.util;

import com.lunazstudios.escondeesconde.EscondeEsconde;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextUtil {
    public static MutableText colorize(String message) {
        // This pattern matches each part of the message that starts with a color code
        Pattern pattern = Pattern.compile("&([0-9a-fklmnor])");
        Logger LOGGER = EscondeEsconde.LOGGER;
        Matcher matcher = pattern.matcher(message);

        MutableText result = Text.empty();
        Style currentStyle = Style.EMPTY; // Hold the current style based on the codes we encounter
        int lastEnd = 0; // Track the end of the last matched segment

        // Loop through all matches
        while (matcher.find()) {
            // Append text before the formatting code as unformatted text
            if (matcher.start() > lastEnd) {
                String textPart = message.substring(lastEnd, matcher.start());
                result.append(Text.literal(textPart).setStyle(currentStyle));
            }

            // Update the lastEnd position
            lastEnd = matcher.end();

            // Apply the new formatting, updating the currentStyle
            char code = matcher.group(1).charAt(0);
            Formatting newFormat = ColorCodes.getFormattingByCode(code);
            if (newFormat.isColor()) {
                currentStyle = Style.EMPTY.withColor(newFormat);
            } else {
                currentStyle = currentStyle.withFormatting(newFormat);
            }

            // Log the processing
//            LOGGER.info("Processing code: '" + code + "', Current style: " + currentStyle);
        }

        // Append any remaining part of the message that doesn't have a formatting code in front of it
        if (lastEnd < message.length()) {
            String textPart = message.substring(lastEnd);
            result.append(Text.literal(textPart).setStyle(currentStyle));
        }

        // Log the final result
//        LOGGER.info("Final result: " + result.getString());

        return result;
    }
}
