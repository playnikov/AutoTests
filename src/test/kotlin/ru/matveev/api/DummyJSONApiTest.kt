package ru.matveev.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertNotNull
import ru.matveev.api.dto.ErrorResponse
import ru.matveev.api.dto.products.Product
import ru.matveev.api.dto.products.ProductsResponse
import ru.matveev.api.dto.users.LoginRequest
import ru.matveev.api.dto.users.LoginResponse
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DummyJSONApiTest {
    companion object {
        private const val BASE_URL = "https://dummyjson.com"
    }

    private lateinit var client: HttpClient

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = false
        explicitNulls = false
    }

    @BeforeAll
    fun setup() {
        client = HttpClient(CIO) {
            engine {
                requestTimeout = 30_000
            }

            expectSuccess = false
            install(ContentNegotiation) {
                json(json)
            }
        }
    }

    @AfterAll
    fun teardown() = client.close()

    /*
    * Позитивный тест: получение списка продуктов
     */
    @Test
    fun `get products with limit and skip return 200`() = runBlocking {
        val limit = 10
        val skip = 10
        val response = client.get("$BASE_URL/products") {
            parameter("limit", limit)
            parameter("skip", skip)
        }
        assertEquals(HttpStatusCode.OK, response.status)

        val body: ProductsResponse = response.body()
        assertTrue(body.products.isNotEmpty())
        assertEquals(limit, body.limit)
        assertEquals(skip, body.skip)
        assertEquals(limit, body.products.size)
    }

    /*
    * Позитивный тест: получение продукта по существующему ID
     */
    @Test
    fun `get a single product by id`() = runBlocking {
        val productId = 1
        val response = client.get("$BASE_URL/products/$productId")
        assertEquals(HttpStatusCode.OK, response.status)

        val product: Product = response.body()
        assertEquals(productId, product.id)
    }

    /*
    * Позитивный тест: создание продукта
     */
    @Test
    fun `create product return 201`() = runBlocking {
        val product = Product(
            title = "Тестовый продукт",
            price = 999.99,
            description = "Это тестовый продукт",
            category = "beauty",
            thumbnail = "https://example.com/thumb.png"
        )

        val response = client.post("$BASE_URL/products/add") {
            contentType(ContentType.Application.Json)
            setBody(product)
        }
        assertEquals(HttpStatusCode.Created, response.status)

        val createdProduct: Product = response.body()
        assertEquals(product.title, createdProduct.title)
        assertEquals(product.price, createdProduct.price, 0.001)
        assertNotNull(createdProduct.id)
    }

    /*
    * Позитивный тест: авторизация пользователя
     */
    @Test
    fun `login with valid credentials`() = runBlocking {
        val response = client.post("$BASE_URL/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(username = "emilys", password = "emilyspass"))
        }

        assertEquals(HttpStatusCode.OK, response.status)

        val body: LoginResponse = response.body()
        assertNotNull(body.accessToken)
        assertNotNull(body.refreshToken)
        assertTrue(body.accessToken.isNotBlank())
    }

    /*
    * Негативный тест: авторизация с неправильным паролем
     */
    @Test
    fun `login with wrong password`() = runBlocking {
        val response = client.post("$BASE_URL/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(username = "emilys", password = "wrongpassword"))
        }

        assertTrue(response.status == HttpStatusCode.BadRequest ||
                response.status == HttpStatusCode.Unauthorized)

        val body: ErrorResponse = response.body()
        assertTrue(body.message.contains("Invalid credentials", ignoreCase = true))
    }

    /*
    * Негативный тест: авторизация с неправильным логином
     */
    @Test
    fun `login with non-existent username`() = runBlocking {
        val response = client.post("$BASE_URL/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(username = "invalid", password = "emilyspass"))
        }

        assertTrue(response.status == HttpStatusCode.BadRequest ||
                response.status == HttpStatusCode.Unauthorized)

        val body: ErrorResponse = response.body()
        assertTrue(body.message.contains("Invalid credentials", ignoreCase = true))
    }

    /*
    * Негативный тест: тело запроса не содержит логин
     */
    @Test
    fun `login without username`() = runBlocking {
        val response = client.post("$BASE_URL/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"password": "emilyspass"}""")
        }

        assertTrue(response.status == HttpStatusCode.BadRequest ||
                response.status == HttpStatusCode.Unauthorized)

        val body: ErrorResponse = response.body()
        assertTrue(body.message.contains("Username and password required", ignoreCase = true))
    }

    /*
    * Негативный тест: тело запроса не содержит пароль
     */
    @Test
    fun `login without password`() = runBlocking {
        val response = client.post("$BASE_URL/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"username": "emilys"}""")
        }

        assertTrue(response.status == HttpStatusCode.BadRequest ||
                response.status == HttpStatusCode.Unauthorized)

        val body: ErrorResponse = response.body()
        assertTrue(body.message.contains("Username and password required", ignoreCase = true))
    }

    /*
    * Негативный тест: тело запроса пустое
     */
    @Test
    fun `login with empty body`() = runBlocking {
        val response = client.post("$BASE_URL/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("{}")
        }

        assertTrue(response.status == HttpStatusCode.BadRequest ||
                response.status == HttpStatusCode.Unauthorized)

        val body: ErrorResponse = response.body()
        assertTrue(body.message.contains("Username and password required", ignoreCase = true))
    }
}