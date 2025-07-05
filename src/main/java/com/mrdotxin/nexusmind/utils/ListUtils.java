package com.mrdotxin.nexusmind.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ListUtils {

    public static List<String> singletonElementOrList(String raw) {
        if (!raw.startsWith("[") && !raw.endsWith("]")) {
            return Collections.singletonList(raw);
        } else {
            return Arrays.stream(raw.split(","))
                    .map(String::trim)
                    .toList();
        }
    }
}
