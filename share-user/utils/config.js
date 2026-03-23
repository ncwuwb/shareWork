// 微信小程序真机调试请改为本机局域网 IP，如 http://192.168.1.5:8080
export const API_BASE = 'http://127.0.0.1:8080'

/**
 * 测试阶段：为 true 时不调用 uni.getLocation，改用订单接口返回的「区域签到中心点」作为签到坐标，
 * 可绕过客户端定位授权，且能通过后端 GEO 半径校验。正式上线前请改为 false。
 */
export const SIGN_USE_AREA_CENTER = true
