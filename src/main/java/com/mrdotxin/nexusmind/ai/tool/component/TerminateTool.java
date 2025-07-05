package com.mrdotxin.nexusmind.ai.tool.component;

import com.mrdotxin.nexusmind.ai.agent.BaseAgent;
import com.mrdotxin.nexusmind.ai.agent.ManusAgent;
import com.mrdotxin.nexusmind.ai.enums.AgentStatusEnum;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

public class TerminateTool {

    private final BaseAgent manusAgent;

    public TerminateTool(BaseAgent manusAgent) {
        this.manusAgent = manusAgent;
    }

    @Tool(description = """  
            Terminate the interaction when the request is met OR if the assistant cannot proceed further with the task.
            "When you have finished all the tasks, call this tool to end the work.
            """)
    public String doTerminate() {
        manusAgent.setStatus(AgentStatusEnum.FINISHED);
        manusAgent.setCurrentStep(0);
        return "任务结束";
    }
}

