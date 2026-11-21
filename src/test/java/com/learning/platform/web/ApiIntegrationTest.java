package com.learning.platform.web;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learning.platform.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/** Сквозные сценарии REST API - коды ответов, валидация, обработка ошибок. */
@AutoConfigureMockMvc
class ApiIntegrationTest extends IntegrationTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper json;

    private ResultActions postJson(String url, String body) throws Exception {
        return mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private long createdId(ResultActions result) throws Exception {
        JsonNode node = json.readTree(result.andReturn().getResponse().getContentAsString());
        return node.get("id").asLong();
    }

    private long createUser(String email, String role) throws Exception {
        return createdId(postJson("/api/users",
                "{\"name\":\"User\",\"email\":\"" + email + "\",\"role\":\"" + role + "\"}").andExpect(status().isCreated()));
    }

    @Test
    void fullLearningFlowThroughApi() throws Exception {
        long teacher = createUser("t@api.local", "TEACHER");
        long student = createUser("s@api.local", "STUDENT");

        long course = createdId(postJson("/api/courses", """
                {"title":"API course","teacherId":%d,"modules":[
                  {"title":"M1","orderIndex":1,"lessons":[{"title":"L1","content":"text"}]}]}
                """.formatted(teacher)).andExpect(status().isCreated()));

        JsonNode structure = json.readTree(mvc.perform(get("/api/courses/{id}/structure", course))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modules", hasSize(1)))
                .andReturn().getResponse().getContentAsString());
        long module = structure.at("/modules/0/id").asLong();
        long lesson = structure.at("/modules/0/lessons/0/id").asLong();

        mvc.perform(post("/api/courses/{id}/enroll", course).param("userId", String.valueOf(student)))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/courses/{id}/enroll", course).param("userId", String.valueOf(student)))
                .andExpect(status().isConflict());
        mvc.perform(get("/api/courses/{id}/students", course)).andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(get("/api/users/{id}/courses", student)).andExpect(jsonPath("$[0].courseId").value(course));

        long assignment = createdId(postJson("/api/lessons/" + lesson + "/assignments",
                "{\"title\":\"HW\",\"maxScore\":100}").andExpect(status().isCreated()));
        long submission = createdId(postJson("/api/assignments/" + assignment + "/submit",
                "{\"studentId\":" + student + ",\"content\":\"answer\"}").andExpect(status().isCreated()));
        postJson("/api/assignments/" + assignment + "/submit", "{\"studentId\":" + student + ",\"content\":\"again\"}")
                .andExpect(status().isConflict());
        mvc.perform(put("/api/submissions/{id}/grade", submission).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"score\":95,\"feedback\":\"ok\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("GRADED"));

        JsonNode quiz = json.readTree(postJson("/api/modules/" + module + "/quiz", """
                {"title":"Quiz","questions":[{"text":"Q1","type":"SINGLE_CHOICE",
                  "options":[{"text":"right","correct":true},{"text":"wrong","correct":false}]}]}
                """).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
        long question = quiz.at("/questions/0/id").asLong();
        long rightOption = quiz.at("/questions/0/options/0/id").asLong();
        postJson("/api/quizzes/" + quiz.get("id").asLong() + "/take",
                "{\"studentId\":" + student + ",\"answers\":{\"" + question + "\":[" + rightOption + "]}}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.score").value(100))
                .andExpect(jsonPath("$.passed").value(true));

        mvc.perform(get("/api/users/{id}/submissions", student)).andExpect(jsonPath("$[0].score").value(95));
        mvc.perform(get("/api/users/{id}/quiz-results", student)).andExpect(jsonPath("$", hasSize(1)));

        mvc.perform(delete("/api/courses/{id}/enroll", course).param("userId", String.valueOf(student)))
                .andExpect(status().isNoContent());
        mvc.perform(delete("/api/courses/{id}", course)).andExpect(status().isNoContent());
        mvc.perform(get("/api/courses/{id}", course)).andExpect(status().isNotFound());
    }

    @Test
    void returns404ForMissingEntities() throws Exception {
        mvc.perform(get("/api/courses/{id}", 999_999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Course with id 999999 not found"));
        mvc.perform(get("/api/users/{id}", 999_999)).andExpect(status().isNotFound());
    }

    @Test
    void returns400WithFieldErrorsOnInvalidInput() throws Exception {
        postJson("/api/users", "{\"name\":\"\",\"email\":\"not-an-email\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.role").exists());
    }

    @Test
    void returns400OnMalformedJsonAndWrongParameterType() throws Exception {
        postJson("/api/courses", "{oops").andExpect(status().isBadRequest());
        mvc.perform(get("/api/courses/{id}", "abc")).andExpect(status().isBadRequest());
    }

    @Test
    void returns409OnDuplicateEmailAndDeletingTeacherWithCourses() throws Exception {
        long teacher = createUser("dup@api.local", "TEACHER");
        postJson("/api/users", "{\"name\":\"X\",\"email\":\"dup@api.local\",\"role\":\"STUDENT\"}")
                .andExpect(status().isConflict());
        postJson("/api/courses", "{\"title\":\"C\",\"teacherId\":" + teacher + "}").andExpect(status().isCreated());

        mvc.perform(delete("/api/users/{id}", teacher)).andExpect(status().isConflict());
    }

    @Test
    void categoriesAndTagsCrud() throws Exception {
        long category = createdId(postJson("/api/categories", "{\"name\":\"Design\"}").andExpect(status().isCreated()));
        mvc.perform(put("/api/categories/{id}", category).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"UX\"}"))
                .andExpect(jsonPath("$.name").value("UX"));
        postJson("/api/categories", "{\"name\":\"ux\"}").andExpect(status().isConflict());
        mvc.perform(delete("/api/categories/{id}", category)).andExpect(status().isNoContent());

        long tag = createdId(postJson("/api/tags", "{\"name\":\"Kotlin\"}").andExpect(status().isCreated()));
        mvc.perform(get("/api/tags")).andExpect(jsonPath("$[?(@.name=='Kotlin')]").exists());
        mvc.perform(delete("/api/tags/{id}", tag)).andExpect(status().isNoContent());
    }
}
