package com.game.controller;

import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class PlayerController {
    @Autowired
    private PlayerService playerService;

    @GetMapping(value = "rest/players")
    public List<Player> getPlayers(String name, String title, Race race, Profession profession, Long after, Long before, Boolean banned, Integer minExperience, Integer maxExperience, Integer minLevel, Integer maxLevel, PlayerOrder order, Integer pageNumber, Integer pageSize) {
        return playerService.getPlayers(name, title, race, profession, after, before, banned, minExperience, maxExperience, minLevel, maxLevel, order, pageNumber, pageSize);
    }

    @DeleteMapping(value = "rest/players/{id}")
    public void deletePlayer(@PathVariable Long id) {
        playerService.deletePlayer(id);
    }

    @GetMapping(value = "/rest/players/{id}")
    public Player getPlayerByID(@PathVariable Long id) {
        return playerService.getPlayer(id);
    }

    @GetMapping(value = "/rest/players/count")
    public int getPlayersCount(String name, String title, Race race, Profession profession, Long after, Long before, Boolean banned, Integer minExperience, Integer maxExperience, Integer minLevel, Integer maxLevel) {
        return playerService.getPlayersCount(name, title, race, profession, after, before, banned, minExperience, maxExperience, minLevel, maxLevel);
    }

    @PostMapping(value = "/rest/players")
    public Player createPlayer(@RequestBody PlayerRequest playerRequest) {
        return playerService.createPlayer(
                playerRequest.getName(),
                playerRequest.getTitle(),
                playerRequest.getRace(),
                playerRequest.getProfession(),
                playerRequest.getBirthday(),
                playerRequest.getBanned(),
                playerRequest.getExperience()
        );
    }

    @PostMapping(value = "/rest/players/{id}")
    public Player updatePlayer(@PathVariable Long id, @RequestBody PlayerRequest playerRequest) {
        return playerService.updatePlayer(
                id,
                playerRequest.getName(),
                playerRequest.getTitle(),
                playerRequest.getRace(),
                playerRequest.getProfession(),
                playerRequest.getBirthday(),
                playerRequest.getBanned(),
                playerRequest.getExperience()
        );
    }
}




