package com.unduck.paletteduck.domain.word.controller;

import com.unduck.paletteduck.domain.word.service.WordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/word")
@RequiredArgsConstructor
public class WordController {

    private final WordService wordService;

    @GetMapping("/random")
    public List<String> getRandomWords(@RequestParam(defaultValue = "3") int count) {
        return wordService.getMixedWords(count);
    }
}