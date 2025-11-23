<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login</title>
    <!-- Bootstrap CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Bootstrap Icons (New Line) -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link href="${url.resourcesPath}/css/styles.css" rel="stylesheet">
</head>
<body>
    <div class="container-fluid">
      <div class="row justify-content-center">
        <div class="col-md-6 col-lg-4">
          <div class="card mt-5">
            <div class="card-header bg-primary text-white">
                <h4 class="mb-0">${kcSanitize(msg("loginTitle", "Cost calc"))?no_esc}</h4>
            </div>
            <div class="card-body">

              <!-- Main Login Form -->
              <form action="${url.loginAction}" method="post">
                <div class="mb-3">
                  <label for="username" class="form-label">${msg("username")}</label>
                  <input type="text" id="username" name="username" class="form-control" autofocus>
                </div>
                <div class="mb-3">
                  <label for="password" class="form-label">${msg("password")}</label>
                  <input type="password" id="password" name="password" class="form-control">
                </div>
                <div class="d-grid">
                  <button type="submit" class="btn btn-primary">${msg("doLogIn")}</button>
                </div>
              </form>

              <!-- START: Social Login Section -->
              <#if realm.password && social.providers??>
                <div class="text-center my-3">
                    <hr class="text-muted">
                    <span class="text-secondary small text-uppercase">Or sign in with</span>
                </div>

                <div class="d-grid gap-2">
                    <#list social.providers as p>
                        <a href="${p.loginUrl}" id="social-${p.alias}" class="btn btn-outline-dark position-relative">
                            <!-- Icon Logic: Maps 'google' -> 'bi-google', 'github' -> 'bi-github' -->
                            <i class="bi bi-${p.alias} position-absolute start-0 ms-3 fs-5"></i>

                            <span>${p.displayName!}</span>
                        </a>
                    </#list>
                </div>
              </#if>
              <!-- END: Social Login Section -->

            </div>
          </div>
        </div>
      </div>
    </div>
</body>
</html>