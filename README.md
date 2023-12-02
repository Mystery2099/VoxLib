# VoxLib
This mod adds some tools which help modders to more easily manipulate, create, and rotate voxel shapes in their code!

## Developers
### Declaring the dependency
```gradle
repositories {
    maven {
        name = "GitHubPackages"
        url = "https://maven.pkg.github.com/Mystery2099/VoxLib"
        credentials {
            username = USERNAME
            password = TOKEN
        }
    }
}

dependencies {
    modImplementation("com.github.mystery2099:voxlib:VERSION")
}
```
Replace `USERNAME` with you GitHub username.

Replace `TOKEN` with a GitHub token.

Replace `VERSION` with the version of the mod you would like to use.
>Example: `1.0.0+1.19.4`

For more information, see [Working with a GitHub Packages Registry](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-gradle-registry#using-a-published-package)