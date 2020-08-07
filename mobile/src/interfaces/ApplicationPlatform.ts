/**
 * This interface is going to keep the platform.
 * If the platform is native android app, then isAndroid is going to be true.
 * If the platform is a mobile PWA then isMobilePWA is going to be true.
 * if the platform is web PWA then isWebPWA is going to be true
 *
 * @author Jagat Bandhu
 * @since 0.0.1
 */
interface ApplicationPlatform{
    isAndroid: boolean,
    isMobilePWA: boolean,
    isWebPWA: boolean
}
