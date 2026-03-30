package com.clubportal.service;

import com.clubportal.model.ClubChatKbEntry;

public interface ClubChatKbSemanticScorer {
    int score(String normalizedMessage, ClubChatKbEntry entry, ChatLanguage language);
}
