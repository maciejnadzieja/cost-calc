
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
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
            </div>
          </div>
        </div>
      </div>
    </div>
</body>
</html>
