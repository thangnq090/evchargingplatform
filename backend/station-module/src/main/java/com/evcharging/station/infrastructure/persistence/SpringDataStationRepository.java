package com.evcharging.station.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Spring Data JPA repository for StationJpaEntity. */
@Repository
public interface SpringDataStationRepository extends JpaRepository<StationJpaEntity, UUID> {

  List<StationJpaEntity> findByVendorId(UUID vendorId);

  List<StationJpaEntity> findByVendorIdAndStatus(UUID vendorId, String status);

  @Query("SELECT s FROM StationJpaEntity s WHERE s.vendorId = :vendorId AND s.deletedAt IS NULL")
  List<StationJpaEntity> findByVendorIdNotDeleted(@Param("vendorId") UUID vendorId);

  @Query(
      "SELECT s FROM StationJpaEntity s WHERE s.vendorId = :vendorId AND s.status = :status AND s.deletedAt IS NULL")
  List<StationJpaEntity> findByVendorIdAndStatusNotDeleted(
      @Param("vendorId") UUID vendorId, @Param("status") String status);

  @Query(
      "SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM StationJpaEntity s "
          + "WHERE s.vendorId = :vendorId AND s.name = :name AND s.deletedAt IS NULL")
  boolean existsByVendorIdAndNameNotDeleted(
      @Param("vendorId") UUID vendorId, @Param("name") String name);

  @Query("SELECT s FROM StationJpaEntity s WHERE s.id = :id AND s.deletedAt IS NULL")
  Optional<StationJpaEntity> findByIdNotDeleted(@Param("id") UUID id);

  @Query(
      value =
          """
      SELECT s.* FROM station.stations s
      WHERE s.deleted_at IS NULL
      AND ST_DWithin(s.location, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography, :radiusMeters)
      ORDER BY ST_Distance(s.location, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography)
      """,
      nativeQuery = true)
  List<StationJpaEntity> findNearby(
      @Param("lat") double lat,
      @Param("lng") double lng,
      @Param("radiusMeters") double radiusMeters);

  @Query("SELECT s FROM StationJpaEntity s WHERE s.id = :id")
  Optional<StationJpaEntity> findByIdIncludingDeleted(@Param("id") UUID id);

  // --- Cursor-paginated queries ---

  @Query(
      "SELECT s FROM StationJpaEntity s WHERE s.vendorId = :vendorId AND s.deletedAt IS NULL"
          + " AND (:createdAt IS NULL OR s.createdAt < :createdAt)"
          + " ORDER BY s.createdAt DESC")
  List<StationJpaEntity> findByVendorIdPaginated(
      @Param("vendorId") UUID vendorId,
      @Param("createdAt") java.time.Instant createdAt,
      org.springframework.data.domain.Pageable pageable);

  @Query(
      "SELECT s FROM StationJpaEntity s WHERE s.vendorId = :vendorId AND s.status = :status AND s.deletedAt IS NULL"
          + " AND (:createdAt IS NULL OR s.createdAt < :createdAt)"
          + " ORDER BY s.createdAt DESC")
  List<StationJpaEntity> findByVendorIdAndStatusPaginated(
      @Param("vendorId") UUID vendorId,
      @Param("status") String status,
      @Param("createdAt") java.time.Instant createdAt,
      org.springframework.data.domain.Pageable pageable);

  // --- Admin all-stations paginated queries ---

  @Query(
      "SELECT s FROM StationJpaEntity s WHERE s.deletedAt IS NULL"
          + " AND (:createdAt IS NULL OR s.createdAt < :createdAt)"
          + " ORDER BY s.createdAt DESC")
  List<StationJpaEntity> findAllPaginated(
      @Param("createdAt") java.time.Instant createdAt,
      org.springframework.data.domain.Pageable pageable);

  @Query(
      "SELECT s FROM StationJpaEntity s WHERE s.status = :status AND s.deletedAt IS NULL"
          + " AND (:createdAt IS NULL OR s.createdAt < :createdAt)"
          + " ORDER BY s.createdAt DESC")
  List<StationJpaEntity> findAllByStatusPaginated(
      @Param("status") String status,
      @Param("createdAt") java.time.Instant createdAt,
      org.springframework.data.domain.Pageable pageable);
}
