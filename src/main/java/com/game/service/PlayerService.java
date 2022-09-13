package com.game.service;

import com.game.controller.BadRequestException;
import com.game.controller.NotFoundException;
import com.game.controller.PlayerOrder;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlayerService {
    @Autowired
    private PlayerRepository repository;

    public List<Player> getPlayers(String name, String title, Race race, Profession profession, Long after, Long before, Boolean banned, Integer minExperience, Integer maxExperience, Integer minLevel, Integer maxLevel, PlayerOrder order, Integer pageNumber, Integer pageSize) {

        List<Player> all = getPlayers(name, title, race, profession, after, before, banned, minExperience, maxExperience, minLevel, maxLevel);

        if (order == PlayerOrder.NAME) {
            all.sort(Comparator.comparing(Player::getName));
        } else if (order == PlayerOrder.BIRTHDAY) {
            all.sort(Comparator.comparing(Player::getBirthday));
        } else if (order == PlayerOrder.LEVEL) {
            all.sort(Comparator.comparing(Player::getLevel));
        } else if (order == PlayerOrder.EXPERIENCE) {
            all.sort(Comparator.comparing(Player::getExperience));
        } else {
            all.sort(Comparator.comparing(Player::getId));
        }

        return getPage(all, pageNumber, pageSize);

    }

    private boolean BirthdayBefore(Player player, Long before) {
        return player.getBirthday().getTime() < before;
    }

    private boolean BirthdayAfter(Player player, Long after) {
        return player.getBirthday().getTime() > after;
    }

    private boolean isLevelLessThan(Player player, Integer maxLevel) {
        return player.getLevel() <= maxLevel;
    }

    private boolean isLevelGreaterThan(Player player, Integer minLevel) {
        return player.getLevel() >= minLevel;
    }

    private boolean isExperienceLessThan(Player player, Integer maxExperience) {
        return player.getExperience() <= maxExperience;
    }

    private List<Player> getPage(List<Player> all, Integer pageNumber, Integer pageSize) {
        pageNumber = pageNumber == null ? 0 : pageNumber;
        pageSize = pageSize == null ? 3 : pageSize;

        int start = pageNumber * pageSize <= all.size() ? pageSize * pageNumber : all.size() - pageSize;
        int end = Math.min((pageSize * pageNumber + pageSize), all.size());
        start = Math.min(start, end);
        all = all.subList(start, end);

        return all;
    }

    private boolean isExperienceGreaterThan(Player player, Integer minExperience) {
        return player.getExperience() >= minExperience;
    }

    private boolean hasProfession(Player player, Profession profession) {
        return player.getProfession() == profession;
    }

    private boolean hasRace(Player player, Race race) {
        return player.getRace() == race;
    }

    private boolean hasName(Player player, String name) {
        return player.getName().toUpperCase().contains(name.toUpperCase());
    }

    public void deletePlayer(Long id) {
        checkValidId(id);
        repository.deleteById(id);
    }

    public boolean hasPlayer(Long id) {
        return repository.existsById(id);
    }

    public Player getPlayer(Long id) {
        checkValidId(id);
        return repository.findById(id).orElseThrow((IllegalStateException::new));

    }

    public List<Player> getPlayers(String name, String title, Race race, Profession profession, Long after, Long before, Boolean banned, Integer minExperience, Integer maxExperience, Integer minLevel, Integer maxLevel) {
        List<Player> all = repository.findAll();
        if (name != null) {
            all = all.stream().filter(player -> hasName(player, name)).collect(Collectors.toList());
        }
        if (title != null) {
            all = all.stream().filter(player -> hasTitle(player, title)).collect(Collectors.toList());
        }
        if (race != null) {
            all = all.stream().filter(player -> hasRace(player, race)).collect(Collectors.toList());
        }
        if (profession != null) {
            all = all.stream().filter(player -> hasProfession(player, profession)).collect(Collectors.toList());
        }
        if (minExperience != null) {
            all = all.stream().filter(player -> isExperienceGreaterThan(player, minExperience)).collect(Collectors.toList());
        }
        if (maxExperience != null) {
            all = all.stream().filter(player -> isExperienceLessThan(player, maxExperience)).collect(Collectors.toList());
        }
        if (minLevel != null) {
            all = all.stream().filter(player -> isLevelGreaterThan(player, minLevel)).collect(Collectors.toList());
        }
        if (maxLevel != null) {
            all = all.stream().filter(player -> isLevelLessThan(player, maxLevel)).collect(Collectors.toList());
        }
        if (banned != null) {
            all = all.stream().filter(player -> isBanned(player, banned)).collect(Collectors.toList());
        }
        if (after != null) {
            all = all.stream().filter(player -> BirthdayAfter(player, after)).collect(Collectors.toList());
        }
        if (before != null) {
            all = all.stream().filter(player -> BirthdayBefore(player, before)).collect(Collectors.toList());
        }
        return all;
    }

    private Boolean isBanned(Player player, Boolean banned) {
        return player.getBanned() == banned;
    }

    public int getPlayersCount(String name, String title, Race race, Profession profession, Long after, Long before, Boolean banned, Integer minExperience, Integer maxExperience, Integer minLevel, Integer maxLevel) {
        List<Player> playersList = getPlayers(name, title, race, profession, after, before, banned, minExperience, maxExperience, minLevel, maxLevel);
        return playersList.size();
    }

    private boolean hasTitle(Player player, String title) {
        return player.getTitle().toUpperCase().contains(title.toUpperCase());
    }

    public Player createPlayer(String name, String title, Race race, Profession profession, Long birthday, Boolean banned, Integer experience) {
        if (isNameInvalid(name)) throw new BadRequestException();
        if (isTitleInvalid(title)) throw new BadRequestException();
        if (race == null) throw new BadRequestException();
        if (profession == null) throw new BadRequestException();
        if (birthday == null || getYear(birthday) < 2000 || getYear(birthday) > 3000) throw new BadRequestException();
        if (experience == null || experience > 10000000 || experience < 0) throw new BadRequestException();
        if (banned == null) banned = false;

        int level = (int) ((Math.sqrt(2500 + 200 * experience) - 50) / (100));
        int untilNextLevel = 50 * (level + 1) * (level + 2) - experience;

        Player player = new Player();
        player.setName(name);
        player.setTitle(title);
        player.setRace(race);
        player.setProfession(profession);
        player.setBirthday(new Date(birthday));
        player.setExperience(experience);
        player.setBanned(banned);
        player.setLevel(level);
        player.setUntilNextLevel(untilNextLevel);

        return repository.save(player);

    }

    private int getYear(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        return calendar.get(Calendar.YEAR);
    }

    private boolean isTitleInvalid(String title) {
        if (title == null) return true;
        return title.length() > 30;
    }

    private boolean isNameInvalid(String name) {
        if (name == null) return true;
        if (name.length() > 12) return true;
        return name.isEmpty();

    }

    public Player updatePlayer(Long id, String name, String title, Race race, Profession profession, Long birthday, Boolean banned, Integer experience) {
        checkValidId(id);
        Player player = repository.findById(id).orElseThrow((IllegalStateException::new));


        if (name != null) player.setName(name);
        if (title != null) player.setTitle(title);
        if (race != null) player.setRace(race);
        if (profession != null) player.setProfession(profession);
        if (birthday != null) {
            if (getYear(birthday) < 2000 || getYear(birthday) > 3000) throw new BadRequestException();
            player.setBirthday(new Date(birthday));
        }
        if (experience != null) {
            if (experience > 10000000 || experience < 0) throw new BadRequestException();
            player.setExperience(experience);

            int level = (int) ((Math.sqrt(2500 + 200 * experience) - 50) / (100));
            int untilNextLevel = 50 * (level + 1) * (level + 2) - experience;

            player.setUntilNextLevel(untilNextLevel);
            player.setLevel(level);
        }
        if (banned != null) player.setBanned(banned);


        return repository.save(player);
    }

    private void checkValidId(Long id) {
        if (id == null || id < 1) {
            throw new BadRequestException();
        }
        if (!hasPlayer(id)) {
            throw new NotFoundException();
        }
    }
}
