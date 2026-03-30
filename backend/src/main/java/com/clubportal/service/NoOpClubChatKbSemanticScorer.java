package com.clubportal.service;

import com.clubportal.model.ClubChatKbEntry;
import org.springframework.stereotype.Service;

@Service
public class NoOpClubChatKbSemanticScorer implements ClubChatKbSemanticScorer {
    @Override
    public int score(String normalizedMessage, ClubChatKbEntry entry, ChatLanguage language) {
        return 0;
    }
}
