package com.rookies3.myspringbootlab.service;

import com.rookies3.myspringbootlab.controller.dto.BookDTO;
import com.rookies3.myspringbootlab.controller.dto.PublisherDTO;
import com.rookies3.myspringbootlab.entity.Book;
import com.rookies3.myspringbootlab.entity.BookDetail;
import com.rookies3.myspringbootlab.entity.Publisher;
import com.rookies3.myspringbootlab.exception.BusinessException;
import com.rookies3.myspringbootlab.exception.ErrorCode;
import com.rookies3.myspringbootlab.repository.BookRepository;

import com.rookies3.myspringbootlab.repository.PublisherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Book 관련 비즈니스 로직을 처리하는 서비스 클래스
 * 도서의 CRUD 작업, 검색, 유효성 검증 등을 담당합니다.
 * BookDetail이 없는 도서도 정상적으로 처리합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookDetailService {

    private final BookRepository bookRepository;
    private final PublisherRepository publisherRepository;

    public List<BookDTO.Response> getAllBooks() {
        return bookRepository.findAll()
                .stream()
                .map(BookDTO.Response::fromEntity)
                .toList();
    }

    /**
     * 모든 도서를 조회합니다.
     * 각 도서의 출판사 정보에는 해당 출판사의 전체 도서 수가 포함됩니다.
     * N+1 문제를 방지하기 위해 출판사별 도서 수를 미리 계산합니다.
     * BookDetail이 없는 도서도 정상적으로 조회됩니다.
     *
     * @return 모든 도서의 응답 DTO 목록
     */
//    public List<BookDTO.Response> getAllBooks() {
//        // 1. 모든 도서 조회
//        List<Book> books = bookRepository.findAll();
//
//        // 2. 출판사별 도서 수를 한 번에 계산 (N+1 문제 방지)
//        // Stream을 사용하여 출판사 ID별로 그룹화하고 각 그룹의 개수를 계산
//        Map<Long, Long> publisherBookCounts = books.stream()
//                .filter(book -> book.getPublisher() != null) // null 체크
//                .collect(Collectors.groupingBy(
//                        book -> book.getPublisher().getId(),
//                        Collectors.counting()
//                ));
//
//        // 3. 각 도서를 DTO로 변환하면서 출판사의 도서 수 정보 설정
//        return books.stream()
//                .map(book -> {
//                    // 기본 DTO 변환 (BookDetail이 null이어도 정상 처리됨)
//                    BookDTO.Response response = BookDTO.Response.fromEntity(book);
//
//                    // 출판사 정보가 있는 경우 bookCount 설정
//                    if (book.getPublisher() != null && response.getPublisher() != null) {
//                        Long publisherId = book.getPublisher().getId();
//                        Long bookCount = publisherBookCounts.getOrDefault(publisherId, 0L);
//
//                        // 출판사 정보에 도서 수 포함하여 새로운 객체 생성
//                        PublisherDTO.SimpleResponse publisherWithCount =
//                                PublisherDTO.SimpleResponse.fromEntityWithCount(
//                                        book.getPublisher(), bookCount);
//
//                        // response 객체의 publisher 필드 업데이트
//                        response.setPublisher(publisherWithCount);
//                    }
//
//                    return response;
//                })
//                .collect(Collectors.toList());
//    }

    /**
     * ID로 특정 도서를 조회합니다.
     * BookDetail이 없는 도서도 정상적으로 조회됩니다.
     * 모든 연관 엔티티(BookDetail, Publisher)를 포함하여 조회하되,
     * 일부가 null일 수 있습니다.
     *
     * @param id 조회할 도서의 ID
     * @return 도서 응답 DTO (BookDetail이 null일 수 있음)
     * @throws BusinessException 도서를 찾을 수 없는 경우
     */
    public BookDTO.Response getBookById(Long id) {
        Book book = bookRepository.findByIdWithAllDetails(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Book", "id", id));

        // 2. 기본 DTO 변환 (BookDetail이 null이어도 정상 처리됨)
        BookDTO.Response response = BookDTO.Response.fromEntity(book);

        // 3. 출판사 정보가 있는 경우 bookCount 설정
        if (book.getPublisher() != null && response.getPublisher() != null) {
            Long publisherId = book.getPublisher().getId();
            Long bookCount = bookRepository.countByPublisherId(publisherId);

            // 출판사 정보에 도서 수 포함
            PublisherDTO.SimpleResponse publisherWithCount =
                    PublisherDTO.SimpleResponse.fromEntityWithCount(
                            book.getPublisher(), bookCount);

            response.setPublisher(publisherWithCount);
        }

        return response;
    }

    /**
     * ISBN으로 특정 도서를 조회합니다.
     * BookDetail이 없는 도서도 정상적으로 조회됩니다.
     * BookDetail 정보도 함께 로딩하되, null일 수 있습니다.
     *
     * @param isbn 조회할 도서의 ISBN
     * @return 도서 응답 DTO (BookDetail이 null일 수 있음)
     * @throws BusinessException 도서를 찾을 수 없는 경우
     */
    public BookDTO.Response getBookByIsbn(String isbn) {
        Book book = bookRepository.findByIsbnWithBookDetail(isbn)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Book", "ISBN", isbn));

        // 2. 기본 DTO 변환 (BookDetail이 null이어도 정상 처리됨)
        BookDTO.Response response = BookDTO.Response.fromEntity(book);

        // 3. 출판사 정보가 있는 경우 bookCount 설정
        if (book.getPublisher() != null && response.getPublisher() != null) {
            Long publisherId = book.getPublisher().getId();
            Long bookCount = bookRepository.countByPublisherId(publisherId);

            PublisherDTO.SimpleResponse publisherWithCount =
                    PublisherDTO.SimpleResponse.fromEntityWithCount(
                            book.getPublisher(), bookCount);

            response.setPublisher(publisherWithCount);
        }

        return response;
    }

    /**
     * 작가명으로 도서를 검색합니다.
     * 부분 일치 검색을 지원하며 대소문자를 구분하지 않습니다.
     * BookDetail이 없는 도서도 검색 결과에 포함됩니다.
     *
     * @param author 검색할 작가명 (부분 문자열 가능)
     * @return 검색된 도서 목록 (BookDetail이 null인 도서 포함)
     */
    public List<BookDTO.Response> getBooksByAuthor(String author) {
        return bookRepository.findByAuthorContainingIgnoreCase(author)
                .stream()
                .map(BookDTO.Response::fromEntity)
                .toList();
    }

    /**
     * 제목으로 도서를 검색합니다.
     * 부분 일치 검색을 지원하며 대소문자를 구분하지 않습니다.
     * BookDetail이 없는 도서도 검색 결과에 포함됩니다.
     *
     * @param title 검색할 제목 (부분 문자열 가능)
     * @return 검색된 도서 목록 (BookDetail이 null인 도서 포함)
     */
    public List<BookDTO.Response> getBooksByTitle(String title) {
        return bookRepository.findByTitleContainingIgnoreCase(title)
                .stream()
                .map(BookDTO.Response::fromEntity)
                .toList();
    }

    /**
     * 특정 출판사의 모든 도서를 조회합니다.
     * BookDetail이 없는 도서도 결과에 포함됩니다.
     *
     * @param publisherId 출판사 ID
     * @return 해당 출판사의 도서 목록 (BookDetail이 null인 도서 포함)
     * @throws BusinessException 출판사를 찾을 수 없는 경우
     */
    public List<BookDTO.Response> getBooksByPublisherId(Long publisherId) {
        // Validate publisher exists
        if (!publisherRepository.existsById(publisherId)) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                    "Publisher", "id", publisherId);
        }

        return bookRepository.findByPublisherId(publisherId)
                .stream()
                .map(BookDTO.Response::fromEntity)
                .toList();
    }

    /**
     * 새로운 도서를 생성합니다.
     * 출판사 존재 여부와 ISBN 중복을 검증한 후 도서와 도서 상세 정보를 저장합니다.
     * BookDetail은 선택사항이며, 제공되지 않으면 null로 저장됩니다.
     *
     * @param request 도서 생성 요청 DTO
     * @return 생성된 도서 응답 DTO
     * @throws BusinessException 출판사를 찾을 수 없거나 ISBN이 중복된 경우
     */
    @Transactional
    public BookDTO.Response createBook(BookDTO.Request request) {
        // Validate publisher exists
        Publisher publisher = publisherRepository.findById(request.getPublisherId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Publisher", "id", request.getPublisherId()));

        // Validate ISBN is not already in use
        if (bookRepository.existsByIsbn(request.getIsbn())) {
            throw new BusinessException(ErrorCode.ISBN_DUPLICATE, request.getIsbn());
        }

        // Create book entity
        Book book = Book.builder()
                .title(request.getTitle())
                .author(request.getAuthor())
                .isbn(request.getIsbn())
                .price(request.getPrice())
                .publishDate(request.getPublishDate())
                .publisher(publisher)
                .build();

        // Create book detail if provided
        if (request.getDetail() != null) {
            BookDetail bookDetail = BookDetail.builder()
                    .description(request.getDetail().getDescription())
                    .language(request.getDetail().getLanguage())
                    .pageCount(request.getDetail().getPageCount())
                    .publisher(request.getDetail().getPublisher())
                    .coverImageUrl(request.getDetail().getCoverImageUrl())
                    .edition(request.getDetail().getEdition())
                    .book(book)
                    .build();

            book.setBookDetail(bookDetail);
        }

        // Save and return the book
        Book savedBook = bookRepository.save(book);
        return BookDTO.Response.fromEntity(savedBook);
    }

    /**
     * 기존 도서 정보를 수정합니다.
     * 출판사 존재 여부와 ISBN 중복(자신 제외)을 검증한 후 정보를 업데이트합니다.
     * BookDetail은 선택사항이며, 기존에 없던 경우 새로 생성할 수 있습니다.
     *
     * @param id 수정할 도서의 ID
     * @param request 도서 수정 요청 DTO
     * @return 수정된 도서 응답 DTO
     * @throws BusinessException 도서나 출판사를 찾을 수 없거나 ISBN이 중복된 경우
     */
    @Transactional
    public BookDTO.Response updateBook(Long id, BookDTO.Request request) {
        // Find the book
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Book", "id", id));

        // Validate publisher exists (if changing)
        Publisher publisher = publisherRepository.findById(request.getPublisherId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Publisher", "id", request.getPublisherId()));

        // Check if another book already has the ISBN
        if (!book.getIsbn().equals(request.getIsbn()) &&
                bookRepository.existsByIsbn(request.getIsbn())) {
            throw new BusinessException(ErrorCode.ISBN_DUPLICATE, request.getIsbn());
        }

        // Update book basic info
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setIsbn(request.getIsbn());
        book.setPrice(request.getPrice());
        book.setPublishDate(request.getPublishDate());
        book.setPublisher(publisher);

        // Update book detail if provided
        if (request.getDetail() != null) {
            BookDetail bookDetail = book.getBookDetail();

            // Create new detail if not exists
            if (bookDetail == null) {
                bookDetail = new BookDetail();
                bookDetail.setBook(book);
                book.setBookDetail(bookDetail);
            }

            // Update detail fields
            bookDetail.setDescription(request.getDetail().getDescription());
            bookDetail.setLanguage(request.getDetail().getLanguage());
            bookDetail.setPageCount(request.getDetail().getPageCount());
            bookDetail.setPublisher(request.getDetail().getPublisher());
            bookDetail.setCoverImageUrl(request.getDetail().getCoverImageUrl());
            bookDetail.setEdition(request.getDetail().getEdition());
        }

        // Save and return updated book
        Book updatedBook = bookRepository.save(book);
        return BookDTO.Response.fromEntity(updatedBook);
    }

    /**
     * 도서를 삭제합니다.
     * Cascade 설정에 의해 연결된 BookDetail도 함께 삭제됩니다.
     * BookDetail이 없는 도서도 정상적으로 삭제됩니다.
     *
     * @param id 삭제할 도서의 ID
     * @throws BusinessException 도서를 찾을 수 없는 경우
     */
    @Transactional
    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Book", "id", id);
        }
        bookRepository.deleteById(id);
    }
}