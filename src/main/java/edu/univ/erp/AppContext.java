package edu.univ.erp;

import edu.univ.erp.auth.AuthService;
import edu.univ.erp.auth.Session;
import edu.univ.erp.service.AdminService;
import edu.univ.erp.service.CatalogService;
import edu.univ.erp.service.EnrollmentService;
import edu.univ.erp.service.GradeService;
import edu.univ.erp.service.ProfileService;

/**
 * One object that holds the current {@link Session} and the service instances the screens use, so
 * a dashboard just reaches for context.enrollmentService() instead of newing things up everywhere.
 * The session is set once, when login succeeds.
 */
public class AppContext {

    private Session session;

    private final AuthService authService = new AuthService();
    private final ProfileService profileService = new ProfileService();
    private final CatalogService catalogService = new CatalogService();
    private final EnrollmentService enrollmentService = new EnrollmentService();
    private final GradeService gradeService = new GradeService();
    private final AdminService adminService = new AdminService();

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public AuthService authService() {
        return authService;
    }

    public ProfileService profileService() {
        return profileService;
    }

    public CatalogService catalogService() {
        return catalogService;
    }

    public EnrollmentService enrollmentService() {
        return enrollmentService;
    }

    public GradeService gradeService() {
        return gradeService;
    }

    public AdminService adminService() {
        return adminService;
    }
}
