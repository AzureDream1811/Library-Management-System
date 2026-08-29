package com.azure.libraryms.book.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.azure.libraryms.book.dto.request.BookCreateRequest;
import com.azure.libraryms.book.dto.request.BookPatchRequest;
import com.azure.libraryms.book.dto.request.BookSearchRequest;
import com.azure.libraryms.book.dto.response.BookResponse;
import com.azure.libraryms.book.service.BookService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    @PostMapping
    public ResponseEntity<BookResponse> createBook(@RequestBody BookCreateRequest request) {
        BookResponse bookResponse = bookService.createBook(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(bookResponse);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<BookResponse>> searchBooks(@RequestBody BookSearchRequest request) {

        return ResponseEntity.ok(bookService.searchBooks(request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<BookResponse> patchUpdateBook(@PathVariable Long id, @RequestBody BookPatchRequest request) {
        BookResponse bookResponse = bookService.patchUpdate(id, request);
        return ResponseEntity.ok(bookResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

}
