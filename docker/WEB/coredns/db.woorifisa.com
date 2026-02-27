$ORIGIN woorifisa.com.
@   IN  SOA ns.woorifisa.com. admin.woorifisa.com. (
        2026022701  ; Serial
        3600        ; Refresh
        600         ; Retry
        86400       ; Expire
        60          ; Minimum TTL
)

; 네임서버
@       IN  NS  ns.woorifisa.com.
ns      IN  A   127.0.0.1

; DNS 라운드 로빈: 같은 도메인에 IP 2개 등록!
api     IN  A   127.0.0.1
api     IN  A   127.0.0.2
