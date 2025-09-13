package com.mrdotxin.nexusmind.ai.tool.component;

import com.mrdotxin.nexusmind.ai.agent.BaseAgent;
import com.mrdotxin.nexusmind.ai.enums.AgentStatusEnum;
import org.springframework.ai.tool.annotation.Tool;

public class TerminateTool {

    private final BaseAgent baseAgent;

    public TerminateTool(BaseAgent baseAgent) {
        this.baseAgent = baseAgent;
    }

    @Tool(description = """  
            Terminate the interaction when the request is met OR if the assistant cannot proceed further with the task.
            "When you have finished all the tasks, call this tool to end the work.
            """)
    public String doTerminate() {
        baseAgent.terminate();
        return "任务结束";
    }
}

