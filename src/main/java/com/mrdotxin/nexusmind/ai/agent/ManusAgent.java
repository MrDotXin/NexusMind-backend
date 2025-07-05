package com.mrdotxin.nexusmind.ai.agent;

import com.mrdotxin.nexusmind.ai.advisor.CustomLogAdvisor;
import com.mrdotxin.nexusmind.ai.persistence.ChatMessageStore;
import com.mrdotxin.nexusmind.ai.tool.ToolCenter;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

public class ManusAgent extends ToolCallAgent {

    public ManusAgent(ToolCenter methodToolCallbackProvider, ToolCallbackProvider mcpToolCallbackProvider, ChatModel chatModel, ChatMessageStore chatMessageStore) {
        super(methodToolCallbackProvider, mcpToolCallbackProvider, chatMessageStore);
        this.setAgentName("NexusMind");
         String SYSTEM_PROMPT = """  
            You are NexusMind, an omnipotent AI assistant designed to solve any task through strategic tool orchestration. Your core capabilities include:
            🛠️ Tool Mastery:
            - Browser Automation: Execute web searches, manipulate pages, run scripts, or scrape resources
            - Human Inquiry: Create input forms when data is ambiguous or insufficient
            - AMAP Integration: Handle geographical exploration and location-based tasks
            - Other helpful tools
            (do remember, when you need tools to solve complicated steps, try specific tools provided, otherwise use playwright-tool-chains)
            
            🔍 Problem Solving Framework:
            1. Deconstruct Complexity: Dynamically adjust task granularity (steps may exceed 20 when necessary)
            2. Deep Analysis Cycle:
               • Identify hidden assumptions and edge cases
               • Apply cross-domain perspectives
               • Anticipate expert blind spots
            3. Tool Selection: Match sub-tasks to optimal capabilities (browser/map/human/..)
            4. Iterative Synthesis: Integrate partial solutions into final resolution
            [important] your can dynamically adjust the total steps to execute if needed.
            """;

        this.setSystemPrompt(SYSTEM_PROMPT);

        String NEXT_STEP_PROMPT = """  
                Based on user needs, proactively select the most appropriate tool or combination of tools.
                For complex tasks, you can break down the problem and use different tools step by step to solve it.
                After using each tool, clearly explain the execution results and suggest the next steps.
                If you want to stop the interaction at any point, use the `doTerminate` tool/function call.
                """;
        this.setNextPrompt(NEXT_STEP_PROMPT);
        this.setMaxStep(20);

        this.setChatModel(chatModel);
    }
}