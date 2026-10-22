package de.happybavarian07.adminpanel.warnings;

import de.happybavarian07.adminpanel.data.PlayerWarning;
import de.happybavarian07.adminpanel.data.PlayerWarningRepository;
import de.happybavarian07.coolstufflib.jpa.utils.EntityQueryBuilder;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** In-memory repository; Mockito cannot mock classes on Java 21 here. */
class FakePlayerWarningRepository implements PlayerWarningRepository {
    final Map<Long, PlayerWarning> rows = new LinkedHashMap<>();
    int playerQueries;
    private long nextId = 1;

    @Override
    public <S extends PlayerWarning> S save(S entity) {
        if (entity.getId() == 0) setId(entity, nextId++);
        rows.put(entity.getId(), entity);
        return entity;
    }

    @Override
    public List<PlayerWarning> findByPlayerUuid(String playerUuid) {
        playerQueries++;
        return rows.values().stream().filter(row -> row.getPlayerUuid().equals(playerUuid)).toList();
    }

    @Override
    public List<PlayerWarning> findByStatus(String status) {
        return rows.values().stream().filter(row -> row.getStatus().name().equals(status)).toList();
    }

    @Override
    public List<PlayerWarning> findByStatusAndExpiresAtBetween(String status, long from, long to) {
        return rows.values().stream()
                .filter(row -> row.getStatus().name().equals(status) && row.getExpiresAt() >= from && row.getExpiresAt() <= to).toList();
    }

    @Override
    public Optional<PlayerWarning> findById(Long id) {
        return Optional.ofNullable(rows.get(id));
    }

    @Override
    public Iterable<PlayerWarning> findAll() {
        return new ArrayList<>(rows.values());
    }

    @Override
    public long count() {
        return rows.size();
    }

    @Override
    public <S extends PlayerWarning> Iterable<S> saveAll(Iterable<S> entities) {
        entities.forEach(this::save);
        return entities;
    }

    @Override
    public boolean existsById(Long id) {
        return rows.containsKey(id);
    }

    @Override
    public Iterable<PlayerWarning> findAllById(Iterable<Long> ids) {
        List<PlayerWarning> found = new ArrayList<>();
        ids.forEach(id -> findById(id).ifPresent(found::add));
        return found;
    }

    @Override
    public void deleteById(Long id) {
        rows.remove(id);
    }

    @Override
    public void delete(PlayerWarning entity) {
        rows.remove(entity.getId());
    }

    @Override
    public void deleteAllById(Iterable<? extends Long> ids) {
        ids.forEach(rows::remove);
    }

    @Override
    public void deleteAll(Iterable<? extends PlayerWarning> entities) {
        entities.forEach(this::delete);
    }

    @Override
    public void deleteAll() {
        rows.clear();
    }

    @Override
    public boolean isDatabaseReady() {
        return true;
    }

    @Override
    public void insert(PlayerWarning entity) {
        save(entity);
    }

    @Override
    public void update(PlayerWarning entity) {
        save(entity);
    }

    @Override
    public EntityQueryBuilder<PlayerWarning> query() {
        throw new UnsupportedOperationException();
    }

    private static void setId(PlayerWarning entity, long id) {
        try {
            Field field = PlayerWarning.class.getDeclaredField("id");
            field.setAccessible(true);
            field.setLong(entity, id);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
