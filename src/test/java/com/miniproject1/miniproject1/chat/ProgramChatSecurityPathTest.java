package com.miniproject1.miniproject1.chat;

import static org.assertj.core.api.Assertions.assertThat;

import com.miniproject1.miniproject1.commons.filter.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;

class ProgramChatSecurityPathTest {

    @Test
    void chatRequiresJwtWhileExistingProgramReadsStayWhitelisted() {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter();

        assertThat(filter.isPath("/api/programs/PBLN_001/chat")).isFalse();
        assertThat(filter.isPath("/api/programs")).isTrue();
        assertThat(filter.isPath("/api/programs/PBLN_001")).isTrue();
    }
}
