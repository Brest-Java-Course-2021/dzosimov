package com.epam.brest.webapp;

import com.epam.brest.model.Book;
import com.epam.brest.model.Genre;
import com.epam.brest.model.sample.BookSample;
import com.epam.brest.model.sample.ReaderSample;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.Arrays;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class ProfileControllerIT {
    private static final String READER_URL = "http://localhost:8060/reader";
    private static final String READER_ID_URL = "http://localhost:8060/reader/1";
    @Autowired
    private WebApplicationContext wac;
    @Autowired
    private RestTemplate restTemplate;
    private MockMvc mockMvc;
    private MockRestServiceServer server;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup(){
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        server = MockRestServiceServer.createServer(restTemplate);
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    public void shouldGetProfilePageWithFoundReaderById() throws Exception {
        ReaderSample rs = createReaderSample();
        server.expect(ExpectedCount.once(), requestTo(READER_ID_URL))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(rs)));
        mockMvc.perform(
                MockMvcRequestBuilders.get("/profile")
                        .sessionAttr("libraryCard", rs.getReaderId())
        ).andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attribute("readerSample",
                        hasProperty("readerId", is(rs.getReaderId()))))
                .andExpect(model().attribute("readerSample",
                        hasProperty("firstName", is(rs.getFirstName()))))
                .andExpect(model().attribute("readerSample",
                        hasProperty("lastName", is(rs.getLastName()))))
                .andExpect(model().attribute("readerSample",
                        hasProperty("patronymic", is(rs.getPatronymic()))))
                .andExpect(model().attribute("readerSample",
                        hasProperty("dateOfRegistry", is(rs.getDateOfRegistry()))))
                .andExpect(model().attribute("readerSample",
                        hasProperty("books", is(rs.getBooks()))));
    }

    @Test
    public void shouldForwardCatalogPageWhenReaderNotFoundById() throws Exception {
        String message = "the reader by id 9999999 not found";
        server.expect(ExpectedCount.once(), requestTo(READER_URL + "/9999999"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(objectMapper.writeValueAsString(null)));
        mockMvc.perform(
                MockMvcRequestBuilders.get("/profile")
                        .sessionAttr("libraryCard", 9999999)
        ).andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(view().name("forward:/catalog"))
                .andExpect(forwardedUrl("/catalog"))
                .andExpect(model().attribute("resultMessage", Matchers.hasToString(message)));
    }

    @Test
    public void shouldGetReaderPageWithFoundReaderById() throws Exception {
        ReaderSample rs = createReaderSample();
        rs.setBooks(null);
        server.expect(ExpectedCount.once(), requestTo(READER_ID_URL + "/without_books"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(objectMapper.writeValueAsString(rs)));
        mockMvc.perform(
                MockMvcRequestBuilders.get("/profile/edit")
                        .sessionAttr("libraryCard", rs.getReaderId())
        ).andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(view().name("reader"))
                .andExpect(model().attribute("readerSample",
                        hasProperty("readerId", is(rs.getReaderId()))))
                .andExpect(model().attribute("readerSample",
                        hasProperty("firstName", is(rs.getFirstName()))))
                .andExpect(model().attribute("readerSample",
                        hasProperty("lastName", is(rs.getLastName()))))
                .andExpect(model().attribute("readerSample",
                        hasProperty("patronymic", is(rs.getPatronymic()))))
                .andExpect(model().attribute("readerSample",
                        hasProperty("dateOfRegistry", is(rs.getDateOfRegistry()))))
                .andExpect(model().attribute("isNew", is(false)));

    }

    @Test
    public void shouldForwardCatalogPageBeforeEditingWhenReaderNotFoundById() throws Exception {
        String message = "the reader by id 9999999 not found";
        server.expect(ExpectedCount.once(), requestTo(READER_URL + "/9999999/without_books"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(objectMapper.writeValueAsString(null)));
        mockMvc.perform(
                MockMvcRequestBuilders.get("/profile/edit")
                        .sessionAttr("libraryCard", 9999999)
        ).andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(view().name("forward:/catalog"))
                .andExpect(forwardedUrl("/catalog"))
                .andExpect(model().attribute("resultMessage", Matchers.hasToString(message)));
    }

    @Test
    public void shouldForwardToProfilePageAfterEditingReaderWithGoodMessage() throws Exception {
        ReaderSample rs = createReaderSample();
        rs.setBooks(null);
        String message = "The reader was edited";
        server.expect(ExpectedCount.once(), requestTo(READER_URL))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("true"));
        mockMvc.perform(
                MockMvcRequestBuilders.post("/profile/edit")
                        .sessionAttr("readerSample", rs)
                        .sessionAttr("libraryCard", rs.getReaderId())
        ).andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.forwardedUrl("/profile"))
                .andExpect(view().name("forward:/profile"))
                .andExpect(model().attribute("resultMessage", Matchers.hasToString(message)));
    }

    @Test
    public void shouldForwardToProfilePageAfterEditingReaderWithBadMessage() throws Exception {
        ReaderSample rs = createReaderSample();
        rs.setBooks(null);
        String message = "The reader was not edited";
        server.expect(ExpectedCount.once(), requestTo(READER_URL))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("false"));
        mockMvc.perform(
                MockMvcRequestBuilders.post("/profile/edit")
                        .sessionAttr("readerSample", rs)
                        .sessionAttr("libraryCard", rs.getReaderId())
        ).andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.forwardedUrl("/profile"))
                .andExpect(view().name("forward:/profile"))
                .andExpect(model().attribute("resultMessage", Matchers.hasToString(message)));
    }

    //TODO: refactor
    @Disabled
    @Test
    public void shouldGetReaderPageBeforeEditingWhenBindingResultHasErrors() throws Exception {
        ReaderSample rs = createReaderSample();
        rs.setFirstName("11");
        rs.setBooks(null);
        mockMvc.perform(
                MockMvcRequestBuilders.post("/profile/edit")
                        .sessionAttr("readerSample", rs)
                        .sessionAttr("libraryCard", rs.getReaderId())
        ).andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(view().name("reader"))
                .andExpect(model().attributeHasFieldErrors("readerSample", "firstName"));
    }

    @Test
    public void shouldGetReaderPage() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.get("/registry")
                        .sessionAttr("bookSample", new BookSample())
        ).andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(view().name("reader"))
                .andExpect(model().attribute("isNew", is(true)))
                .andExpect(model().attribute("readerSample",
                        hasProperty("firstName", Matchers.nullValue())));
    }

    @Test
    public void shouldForwardToCatalogAfterAddingReaderWithGoodMessage() throws Exception {
        ReaderSample rs = new ReaderSample("first", "last", "patronymic");
        ReaderSample savedRs = createReaderSample();
        String message = "The reader was added, library card is 1";
        server.expect(ExpectedCount.once(), requestTo(READER_URL))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(objectMapper.writeValueAsString(savedRs)));
        mockMvc.perform(
                MockMvcRequestBuilders.post("/registry")
                        .sessionAttr("readerSample", rs)
        ).andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.forwardedUrl("/catalog"))
                .andExpect(view().name("forward:/catalog"))
                .andExpect(model().attribute("resultMessage", Matchers.hasToString(message)));
    }

    @Test
    public void shouldForwardToCatalogAfterAddingReaderWithBadMessage() throws Exception {
        ReaderSample rs = new ReaderSample("first", "last", "patronymic");
        ReaderSample savedRs = null;
        String message = "The reader was not added";
        server.expect(ExpectedCount.once(), requestTo(READER_URL))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(objectMapper.writeValueAsString(savedRs)));
        mockMvc.perform(
                MockMvcRequestBuilders.post("/registry")
                        .sessionAttr("readerSample", rs)
        ).andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.forwardedUrl("/catalog"))
                .andExpect(view().name("forward:/catalog"))
                .andExpect(model().attribute("resultMessage", Matchers.hasToString(message)));
    }

    //TODO:refactor
    @Disabled
    @Test
    public void shouldGetReaderPageBeforeAddingWhenBindingResultHasErrors() throws Exception {
        ReaderSample rs = new ReaderSample("12", "", "patronymic");
        mockMvc.perform(
                MockMvcRequestBuilders.post("/registry")
                        .sessionAttr("readerSample", rs)
        ).andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(view().name("reader"))
                .andExpect(model().attributeHasFieldErrors("readerSample",
                        "firstName", "lastName"));
    }

    @Test
    public void shouldForwardToCatalogAfterDeletingReaderWithGoodMessage() throws Exception {
        String message = "The reader was removed";
        server.expect(ExpectedCount.once(), requestTo(READER_ID_URL))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("true"));
        mockMvc.perform(
                MockMvcRequestBuilders.post("/profile/delete")
                        .sessionAttr("libraryCard", 1)
        ).andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.forwardedUrl("/catalog"))
                .andExpect(view().name("forward:/catalog"))
                .andExpect(model().attribute("resultMessage", Matchers.hasToString(message)))
                .andExpect(MockMvcResultMatchers.request().sessionAttributeDoesNotExist("libraryCard"));
    }

    @Test
    public void shouldForwardToCatalogAfterDeletingReaderWithBadMessage() throws Exception {
        String message = "The reader was not removed";
        server.expect(ExpectedCount.once(), requestTo(READER_ID_URL))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("false"));
        mockMvc.perform(
                MockMvcRequestBuilders.post("/profile/delete")
                        .sessionAttr("libraryCard", 1)
        ).andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.forwardedUrl("/catalog"))
                .andExpect(view().name("forward:/catalog"))
                .andExpect(model().attribute("resultMessage", Matchers.hasToString(message)))
                .andExpect(MockMvcResultMatchers.request().sessionAttribute("libraryCard", is(1)));
    }

    @Test
    public void shouldForwardToCatalogAfterRestoringReaderWithGoodMessage() throws Exception {
        String message = "The reader was restored, library card is 1";
        server.expect(ExpectedCount.once(), requestTo(READER_ID_URL))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("true"));
        mockMvc.perform(
                MockMvcRequestBuilders.get("/restore/1")
        ).andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.forwardedUrl("/catalog"))
                .andExpect(view().name("forward:/catalog"))
                .andExpect(model().attribute("resultMessage", Matchers.hasToString(message)));
    }

    @Test
    public void shouldForwardToCatalogAfterRestoringReaderWithBadMessage() throws Exception {
        String message = "The reader was not restored";
        server.expect(ExpectedCount.once(), requestTo(READER_ID_URL))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("false"));
        mockMvc.perform(
                MockMvcRequestBuilders.get("/restore/1")
        ).andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.forwardedUrl("/catalog"))
                .andExpect(view().name("forward:/catalog"))
                .andExpect(model().attribute("resultMessage", Matchers.hasToString(message)));
    }

    @Test
    public void shouldRedirectToCatalogAfterDeletingBookFromBooksListOfReader() throws Exception {
        server.expect(ExpectedCount.once(), requestTo("http://localhost:8060/book/1/reader/1"))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("true"));
        mockMvc.perform(
                MockMvcRequestBuilders.post("/profile/book/delete/1")
                        .sessionAttr("libraryCard", 1)
        ).andDo(MockMvcResultHandlers.print())
                .andExpect(status().isFound())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/profile"))
                .andExpect(view().name("redirect:/profile"));
    }

    private ReaderSample createReaderSample() {
        ReaderSample readerSample = new ReaderSample();
        readerSample.setReaderId(1);
        readerSample.setFirstName("first");
        readerSample.setLastName("last");
        readerSample.setPatronymic("patronymic");
        readerSample.setDateOfRegistry(LocalDate.now());

        Book bs1 = new Book(1,"author", "title", Genre.MYSTERY, 1);
        Book bs2 = new Book(2,"author two", "title two", Genre.MYSTERY, 1);
        Book bs3 = new Book(3,"author three", "title three", Genre.MYSTERY, 1);
        readerSample.setBooks(Arrays.asList(bs1, bs2, bs3));
        return readerSample;
    }

}
