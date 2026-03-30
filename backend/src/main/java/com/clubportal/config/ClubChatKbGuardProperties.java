package com.clubportal.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "app.chat.kb.guard")
public class ClubChatKbGuardProperties {

    private List<String> highRiskKeywords = new ArrayList<>(List.of(
            "退款",
            "退钱",
            "退费",
            "投诉",
            "赔偿",
            "争议",
            "法律",
            "律师",
            "报警",
            "起诉",
            "人工客服",
            "人工处理",
            "特殊情况",
            "refund",
            "complaint",
            "dispute",
            "legal",
            "lawyer",
            "human agent",
            "special case"
    ));

    private List<String> realtimeKeywords = new ArrayList<>(List.of(
            "今天",
            "现在",
            "目前",
            "当前",
            "此刻",
            "临时",
            "刚刚",
            "还剩",
            "还有位置",
            "名额",
            "空位",
            "现在还能",
            "today",
            "now",
            "currently",
            "available now",
            "remaining",
            "left",
            "available today"
    ));

    public List<String> getHighRiskKeywords() {
        return highRiskKeywords;
    }

    public void setHighRiskKeywords(List<String> highRiskKeywords) {
        this.highRiskKeywords = highRiskKeywords == null ? new ArrayList<>() : new ArrayList<>(highRiskKeywords);
    }

    public List<String> getRealtimeKeywords() {
        return realtimeKeywords;
    }

    public void setRealtimeKeywords(List<String> realtimeKeywords) {
        this.realtimeKeywords = realtimeKeywords == null ? new ArrayList<>() : new ArrayList<>(realtimeKeywords);
    }
}
