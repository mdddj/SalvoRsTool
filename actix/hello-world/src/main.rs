use actix_web::{
    delete, get, head, patch, post, put, web, App, HttpRequest, HttpResponse, HttpServer, Responder,
};
use serde::{Deserialize, Serialize};

// 数据模型
#[derive(Serialize, Deserialize)]
struct User {
    id: u32,
    name: String,
    email: String,
}

#[derive(Serialize, Deserialize)]
struct CreateUserRequest {
    name: String,
    email: String,
}                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   

#[get("/")]
async fn hello() -> impl Responder {
    HttpResponse::Ok().body("Hello world!")
}

#[get("/users/{id}")]
async fn get_user(path: web::Path<u32>) -> impl Responder {
    let id = path.into_inner();
    // 模拟从数据库获取用户
    let user = User {
        id,
        name: format!("User {}", id),
        email: format!("user{}@example.com", id),
    };
    HttpResponse::Ok().json(user)
}

#[post("/users")]
async fn create_user(user_req: web::Json<CreateUserRequest>) -> impl Responder {
    // 模拟创建用户
    let new_user = User {
        id: 1,
        name: user_req.name.clone(),
        email: user_req.email.clone(),
    };
    HttpResponse::Created().json(new_user)
}

#[put("/users/{id}")]
async fn update_user(
    path: web::Path<u32>,
    user_req: web::Json<CreateUserRequest>,
) -> impl Responder {
    let id = path.into_inner();
    // 模拟更新用户
    let updated_user = User {
        id,
        name: user_req.name.clone(),
        email: user_req.email.clone(),
    };
    HttpResponse::Ok().json(updated_user)
}

#[patch("/users/{id}")]
async fn patch_user(
    path: web::Path<u32>,
    user_req: web::Json<CreateUserRequest>,
) -> impl Responder {
    let id = path.into_inner();
    // 模拟部分更新用户
    let patched_user = User {
        id,
        name: user_req.name.clone(),
        email: user_req.email.clone(),
    };
    HttpResponse::Ok().json(patched_user)
}

#[delete("/users/{id}")]
async fn delete_user(path: web::Path<u32>) -> impl Responder {
    let _id = path.into_inner();
    // 模拟删除用户
    HttpResponse::NoContent().finish()
}

#[head("/users/{id}")]
async fn head_user(path: web::Path<u32>) -> impl Responder {
    let id = path.into_inner();
    // 返回头部信息，不包含响应体
    HttpResponse::Ok()
        .append_header(("X-User-Exists", "true"))
        .append_header(("X-User-ID", id.to_string()))
        .finish()
}

// 手动路由示例 - GET 方法
async fn manual_hello() -> impl Responder {
    HttpResponse::Ok().body("Hey there!")
}

// 手动路由示例 - POST 方法
async fn manual_echo(req_body: String) -> impl Responder {
    HttpResponse::Ok().body(req_body)
}

// 获取所有用户 (示例)
async fn get_all_users() -> impl Responder {
    let users = vec![
        User {
            id: 1,
            name: "Alice".to_string(),
            email: "alice@example.com".to_string(),
        },
        User {
            id: 2,
            name: "Bob".to_string(),
            email: "bob@example.com".to_string(),
        },
    ];
    HttpResponse::Ok().json(users)
}

// OPTIONS 方法示例 - 获取支持的 HTTP 方法
async fn options_handler(_req: HttpRequest) -> impl Responder {
    HttpResponse::Ok()
        .append_header(("Allow", "GET, POST, PUT, PATCH, DELETE, HEAD, OPTIONS"))
        .finish()
}

#[actix_web::main]
async fn main() -> std::io::Result<()> {
    println!("Starting server at http://127.0.0.1:8080");

    HttpServer::new(|| {
        App::new()
            // 使用属性宏定义的路由
            .service(hello)
            .service(get_user)
            .service(create_user)
            .service(update_user)
            .service(patch_user)
            .service(delete_user)
            .service(head_user)
            // 手动定义的路由
            .route("/hey", web::get().to(manual_hello))
            .route("/echo", web::post().to(manual_echo))
            .route("/users", web::get().to(get_all_users))
            .route("/options", web::get().to(options_handler))
            // 默认路由处理 404
            .default_service(
                web::route().to(|| async { HttpResponse::NotFound().body("Not Found") }),
            )
    })
    .bind(("127.0.0.1", 8080))?
    .run()
    .await
}
