package com.rookies3.myspringbootlab.repository;

import com.rookies3.myspringbootlab.entity.Publisher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PublisherRepository extends JpaRepository<Publisher, Long> {
    /**
     * 출판사 이름으로 특정 출판사를 조회합니다.
     * 출판사 이름은 고유하므로 단일 결과를 반환합니다.
     *
     * @param name 조회할 출판사의 이름
     * @return 해당 이름의 출판사 (Optional로 래핑됨)
     */
    Optional<Publisher> findByName(String name);

    /**
     * ID로 출판사를 조회하면서 해당 출판사의 모든 도서를 즉시 로딩합니다.
     * Fetch Join을 사용하여 N+1 문제를 방지합니다.
     * LEFT JOIN FETCH를 사용하여 도서가 없는 출판사도 조회되도록 합니다.
     *
     * @param id 조회할 출판사의 ID
     * @return 출판사와 도서 목록 (Optional로 래핑됨)
     */
    @Query("SELECT p FROM Publisher p LEFT JOIN FETCH p.books WHERE p.id = :id")
    Optional<Publisher> findByIdWithBooks(@Param("id") Long id);

    /**
     * 특정 이름의 출판사가 존재하는지 확인합니다.
     * 출판사 생성 시 이름 중복 검사에 사용됩니다.
     *
     * @param name 확인할 출판사 이름
     * @return 존재 여부 (true: 존재, false: 미존재)
     */
    boolean existsByName(String name);
}