package com.mrdotxin.nexusmind.ai.tool.component;

import java.util.Scanner;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

public class AskHumanTool {

    @Tool(description = "Ask human if you've encountered problems cannot solved with provided context")
    HumanAnswerResponse askHuman(
            @ToolParam(description = "your question, you are required to use json format for example: {name: \"\", password: \"\"}") String question
    ) {
        return new HumanAnswerResponse("you should use search tool and browser tool for the problem");
    }

    record HumanAnswerResponse(String humanAnswer) {};

    private HumanAnswerResponse askHumanByConsole(String question) {
            if (question == null || question.trim().isEmpty()) {
                return new HumanAnswerResponse("问题不能为空");
            }

            try {
                System.out.println("【需要人类协助】" + question);
                System.out.print("请输入回答: ");

                try (Scanner scanner = new Scanner(System.in)) {
                    String userAnswer = scanner.nextLine().trim();

                    if (userAnswer.isEmpty()) {
                        return new HumanAnswerResponse("您没有提供回答，请使用搜索工具或浏览器工具解决问题");
                    }

                    return new HumanAnswerResponse(userAnswer);
                }
            } catch (Exception e) {
                System.err.println("获取用户回答时发生错误: " + e.getMessage());
                return new HumanAnswerResponse("获取回答失败: " + e.getMessage());
            }
    }
}
