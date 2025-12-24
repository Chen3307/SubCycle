package com.subcycle.service;

import com.subcycle.dto.SubscriptionTemplateResponse;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SubscriptionTemplateService {

    private static final List<SubscriptionTemplateResponse> TEMPLATES = Arrays.asList(
            // 影音娛樂
            new SubscriptionTemplateResponse("Netflix", "影音娛樂", "play-circle", "#E50914", "串流影音平台", "monthly"),
            new SubscriptionTemplateResponse("Disney+", "影音娛樂", "play-circle", "#113CCF", "迪士尼串流平台", "monthly"),
            new SubscriptionTemplateResponse("YouTube Premium", "影音娛樂", "play-circle", "#FF0000", "YouTube 無廣告服務",
                    "monthly"),
            new SubscriptionTemplateResponse("Spotify", "影音娛樂", "music", "#1DB954", "音樂串流平台", "monthly"),
            new SubscriptionTemplateResponse("Apple Music", "影音娛樂", "music", "#FA243C", "Apple 音樂服務", "monthly"),
            new SubscriptionTemplateResponse("KKBOX", "影音娛樂", "music", "#00D9FF", "華語音樂串流平台", "monthly"),
            new SubscriptionTemplateResponse("Podcast (Patreon/Firstory)", "影音娛樂", "podcast", "#FF5000", "Podcast 訂閱服務",
                    "monthly"),

            // 工作生產力
            new SubscriptionTemplateResponse("Google One (Drive)", "工作生產力", "cloud", "#4285F4", "Google 雲端儲存",
                    "monthly"),
            new SubscriptionTemplateResponse("iCloud", "工作生產力", "cloud", "#3693F3", "Apple 雲端儲存", "monthly"),
            new SubscriptionTemplateResponse("Dropbox", "工作生產力", "cloud", "#0061FF", "雲端儲存服務", "monthly"),
            new SubscriptionTemplateResponse("ChatGPT Plus", "工作生產力", "cpu", "#10A37F", "AI 對話助手", "monthly"),
            new SubscriptionTemplateResponse("Midjourney", "工作生產力", "image", "#F5B800", "AI 圖像生成", "monthly"),
            new SubscriptionTemplateResponse("Claude", "工作生產力", "cpu", "#D97757", "AI 助手服務", "monthly"),
            new SubscriptionTemplateResponse("Microsoft 365", "工作生產力", "briefcase", "#D83B01", "Office 辦公軟體",
                    "monthly"),
            new SubscriptionTemplateResponse("Adobe Creative Cloud", "工作生產力", "palette", "#FF0000", "Adobe 創意軟體",
                    "monthly"),
            new SubscriptionTemplateResponse("Canva Pro", "工作生產力", "palette", "#00C4CC", "線上設計工具", "monthly"),
            new SubscriptionTemplateResponse("Notion", "工作生產力", "file-text", "#000000", "協作筆記工具", "monthly"),
            new SubscriptionTemplateResponse("Evernote", "工作生產力", "file-text", "#00A82D", "筆記管理工具", "monthly"),

            // 生活與購物
            new SubscriptionTemplateResponse("Uber One", "生活與購物", "car", "#000000", "Uber 會員服務", "monthly"),
            new SubscriptionTemplateResponse("foodpanda pro", "生活與購物", "shopping-bag", "#D70F64", "外送會員服務", "monthly"),
            new SubscriptionTemplateResponse("Amazon Prime", "生活與購物", "shopping-bag", "#FF9900", "Amazon 會員",
                    "monthly"),
            new SubscriptionTemplateResponse("Coupang (酷澎) Rocket", "生活與購物", "shopping-bag", "#F94A2D", "韓國購物平台會員",
                    "monthly"),
            new SubscriptionTemplateResponse("好市多會員", "生活與購物", "shopping-cart", "#0066B2", "Costco 會員", "yearly"),
            new SubscriptionTemplateResponse("健身房會員", "生活與購物", "activity", "#10B981", "健身房月費", "monthly"),

            // 遊戲與社群
            new SubscriptionTemplateResponse("Xbox Game Pass", "遊戲與社群", "gamepad", "#107C10", "Xbox 遊戲訂閱", "monthly"),
            new SubscriptionTemplateResponse("PlayStation Plus", "遊戲與社群", "gamepad", "#003791", "PS 線上服務", "monthly"),
            new SubscriptionTemplateResponse("Nintendo Switch Online", "遊戲與社群", "gamepad", "#E60012", "Switch 線上服務",
                    "monthly"),
            new SubscriptionTemplateResponse("Twitter (X) Premium", "遊戲與社群", "twitter", "#1DA1F2", "X 進階功能", "monthly"),
            new SubscriptionTemplateResponse("Discord Nitro", "遊戲與社群", "message-circle", "#5865F2", "Discord 進階服務",
                    "monthly"),

            // 其他項目
            new SubscriptionTemplateResponse("手機電信費", "其他項目", "smartphone", "#F59E0B", "手機月租費", "monthly"),
            new SubscriptionTemplateResponse("家用網路/第四台", "其他項目", "wifi", "#F59E0B", "網路電視費用", "monthly"),
            new SubscriptionTemplateResponse("電子報/雜誌訂閱", "其他項目", "book-open", "#F59E0B", "數位內容訂閱", "monthly"));

    /**
     * 獲取所有訂閱模板
     */
    public List<SubscriptionTemplateResponse> getAllTemplates() {
        return TEMPLATES;
    }

    /**
     * 根據類別名稱獲取訂閱模板
     */
    public List<SubscriptionTemplateResponse> getTemplatesByCategory(String categoryName) {
        return TEMPLATES.stream()
                .filter(template -> template.getCategoryName().equals(categoryName))
                .collect(Collectors.toList());
    }

    /**
     * 根據名稱搜尋訂閱模板
     */
    public List<SubscriptionTemplateResponse> searchTemplates(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return TEMPLATES;
        }
        String lowerKeyword = keyword.toLowerCase();
        return TEMPLATES.stream()
                .filter(template -> template.getName().toLowerCase().contains(lowerKeyword) ||
                        template.getDescription().toLowerCase().contains(lowerKeyword))
                .collect(Collectors.toList());
    }
}
