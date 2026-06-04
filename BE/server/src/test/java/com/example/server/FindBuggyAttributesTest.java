package com.example.server;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class FindBuggyAttributesTest {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void findBuggy() {
        String[] tables = {"thuong_hieu", "loai_giay", "mau_sac", "kich_co", "de_giay", "co_giay", "chat_lieu_giay", "trong_luong", "cong_nghe_dem"};
        for (String table : tables) {
            System.out.println("Checking table: " + table);
            try {
                List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT * FROM " + table + " WHERE trang_thai IS NULL OR ten IS NULL OR LTRIM(RTRIM(ten)) = ''");
                for (Map<String, Object> row : rows) {
                    System.out.println("Buggy row found: " + row);
                }
            } catch (Exception e) {
                // Ignore if 'ten' doesn't exist
            }
        }
    }
}
