package com.rookies3.myspringbootlab.repository;

import com.rookies3.myspringbootlab.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Book 엔티티에 대한 데이터 액세스 레이어
 * JpaRepository를 상속받아 기본적인 CRUD 기능을 제공받고,
 * 추가적인 커스텀 쿼리 메서드들을 정의합니다.
 */
@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    /**
     * ISBN으로 도서를 조회합니다.
     * ISBN은 도서의 고유 식별자이므로 단일 결과를 반환합니다.
     *
     * @param isbn 조회할 도서의 ISBN
     * @return 해당 ISBN의 도서 (Optional로 래핑됨)
     */
    Optional<Book> findByIsbn(String isbn);

    /**
     * 작가명에 특정 문자열이 포함된 도서들을 대소문자 구분 없이 검색합니다.
     * Like 검색을 수행하며, 부분 일치도 허용합니다.
     *
     * @param author 검색할 작가명 (부분 문자열)
     * @return 조건에 맞는 도서 목록
     */
    List<Book> findByAuthorContainingIgnoreCase(String author);

    /**
     * 제목에 특정 문자열이 포함된 도서들을 대소문자 구분 없이 검색합니다.
     * Like 검색을 수행하며, 부분 일치도 허용합니다.
     *
     * @param title 검색할 제목 (부분 문자열)
     * @return 조건에 맞는 도서 목록
     */
    List<Book> findByTitleContainingIgnoreCase(String title);

    /**
     * ID로 도서를 조회하면서 BookDetail을 즉시 로딩합니다.
     * LEFT JOIN FETCH를 사용하여 BookDetail이 없는 도서도 조회됩니다.
     * N+1 문제를 방지하고 BookDetail이 null일 수 있는 상황을 처리합니다.
     *
     * @param id 조회할 도서의 ID
     * @return 도서와 상세정보 (BookDetail이 없을 수 있음, Optional로 래핑됨)
     */
    @Query("SELECT b FROM Book b LEFT JOIN FETCH b.bookDetail WHERE b.id = :id")
    Optional<Book> findByIdWithBookDetail(@Param("id") Long id);

    /**
     * ID로 도서를 조회하면서 BookDetail과 Publisher를 모두 즉시 로딩합니다.
     * 모든 연관 엔티티를 한 번의 쿼리로 로딩하여 성능을 최적화합니다.
     * LEFT JOIN FETCH를 사용하여 BookDetail이나 Publisher가 없어도 조회됩니다.
     *
     * @param isbn 조회할 도서의 ID
     * @return 도서, 상세정보, 출판사 정보 (일부가 null일 수 있음, Optional로 래핑됨)
     */
    @Query("SELECT b FROM Book b LEFT JOIN FETCH b.bookDetail WHERE b.isbn = :isbn")
    Optional<Book> findByIsbnWithBookDetail(@Param("isbn") String isbn);

    boolean existsByIsbn(String isbn);

    //Publisher 관련 새로 추가된 메서드
    /**
     * 특정 출판사의 모든 도서를 조회합니다.
     * 출판사 ID를 기준으로 해당 출판사에서 발행한 모든 도서를 반환합니다.
     *
     * @param publisherId 출판사 ID
     * @return 해당 출판사의 도서 목록
     */
    List<Book> findByPublisherId(Long publisherId);

    /**
     * 특정 출판사의 도서 수를 계산합니다.
     * 출판사의 도서 수량을 효율적으로 조회하기 위해 COUNT 쿼리를 사용합니다.
     *
     * @param publisherId 출판사 ID
     * @return 해당 출판사의 도서 수
     */
    @Query("SELECT COUNT(b) FROM Book b WHERE b.publisher.id = :publisherId")
    Long countByPublisherId(@Param("publisherId") Long publisherId);

    /**
     * ID로 도서를 조회하면서 BookDetail과 Publisher를 모두 즉시 로딩합니다.
     * 모든 연관 엔티티를 한 번의 쿼리로 로딩하여 성능을 최적화합니다.
     * LEFT JOIN FETCH를 사용하여 BookDetail이나 Publisher가 없어도 조회됩니다.
     *
     * @param id 조회할 도서의 ID
     * @return 도서, 상세정보, 출판사 정보 (일부가 null일 수 있음, Optional로 래핑됨)
     */
    @Query("SELECT b FROM Book b LEFT JOIN FETCH b.bookDetail LEFT JOIN FETCH b.publisher WHERE b.id = :id")
    Optional<Book> findByIdWithAllDetails(@Param("id") Long id);

}