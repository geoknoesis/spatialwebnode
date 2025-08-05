# Documentation Setup Guide

This guide explains how to set up, maintain, and contribute to the hstpd documentation.

## Documentation Structure

The documentation is organized using GitBook and follows a clear hierarchical structure:

```
docs/
├── .gitbook.yaml              # GitBook configuration
├── SUMMARY.md                 # Table of contents
├── README.md                  # Landing page
├── getting-started/           # New user guides
├── concepts/                  # Core concepts and theory
├── architecture/              # System architecture
├── user-guide/                # Usage instructions
├── developer-guide/           # Development and contribution
├── deployment/                # Deployment guides
├── examples/                  # Code examples
└── reference/                 # API and configuration reference
```

## Setting Up GitBook Locally

### 1. Install GitBook CLI

```bash
# Install GitBook CLI globally
npm install -g gitbook-cli

# Install GitBook
gitbook fetch latest
```

### 2. Initialize Documentation

```bash
cd docs
gitbook init
```

### 3. Serve Documentation Locally

```bash
# Serve documentation on localhost:4000
gitbook serve

# Build static site
gitbook build
```

## Writing Guidelines

### Markdown Standards

- Use **GitHub Flavored Markdown**
- Include a title (H1) at the top of each file
- Use descriptive headings (H2, H3, etc.)
- Keep line length under 80 characters where possible
- Use code blocks with language specification

### Code Examples

```markdown
```kotlin
// Kotlin code example
fun main() {
    println("Hello, hstpd!")
}
```

```yaml
# YAML configuration example
nodeId: "did:example:node:1"
name: "My hstpd Node"
```

```bash
# Shell command example
./gradlew :node:run
```

### File Naming

- Use lowercase with hyphens: `getting-started.md`
- Use descriptive names: `docker-deployment.md`
- Include README.md in each directory for overview

### Content Guidelines

1. **Start with the problem**: What does this solve?
2. **Provide context**: Why is this important?
3. **Show examples**: Include working code examples
4. **Link related content**: Cross-reference other sections
5. **Keep it updated**: Maintain with code changes

## Documentation Workflow

### Adding New Content

1. **Create the file** in the appropriate directory
2. **Add to SUMMARY.md** in the correct section
3. **Write the content** following guidelines
4. **Test locally** with `gitbook serve`
5. **Submit PR** for review

### Updating Existing Content

1. **Identify the file** to update
2. **Make changes** following guidelines
3. **Test locally** to ensure formatting
4. **Update any cross-references**
5. **Submit PR** for review

### Review Process

1. **Content review**: Accuracy and completeness
2. **Technical review**: Code examples and accuracy
3. **Formatting review**: Markdown and GitBook formatting
4. **Link review**: Ensure all links work
5. **Merge and deploy**

## GitBook Features

### Plugins Used

- **search**: Full-text search functionality
- **copy-code-button**: Easy code copying
- **highlight**: Syntax highlighting
- **prism**: Advanced syntax highlighting
- **github**: GitHub integration
- **anchors**: Automatic anchor links
- **emoji**: Emoji support
- **ga**: Google Analytics integration
- **sitemap**: SEO sitemap generation

### Custom Configuration

The `.gitbook.yaml` file configures:
- Plugin settings
- Theme customization
- Syntax highlighting languages
- GitHub integration
- Analytics tracking

## Deployment

### GitHub Pages

1. **Build the documentation**:
   ```bash
   cd docs
   gitbook build
   ```

2. **Deploy to GitHub Pages**:
   - Enable GitHub Pages in repository settings
   - Set source to `/docs` directory
   - Configure custom domain if needed

### Custom Domain

1. **Add CNAME file** to `docs/` directory
2. **Configure DNS** to point to GitHub Pages
3. **Enable HTTPS** in repository settings

## Maintenance

### Regular Tasks

- **Weekly**: Review and update examples
- **Monthly**: Check all links and references
- **Quarterly**: Review and update architecture diagrams
- **Release**: Update version numbers and changelog

### Quality Assurance

- **Spell check**: Use markdown spell checkers
- **Link validation**: Ensure all links work
- **Code validation**: Test all code examples
- **Screenshot updates**: Keep screenshots current

## Contributing

### Documentation Issues

- Use the `documentation` label for doc-related issues
- Provide specific file paths and sections
- Include screenshots for visual issues
- Suggest improvements when reporting problems

### Documentation PRs

- Follow the writing guidelines
- Include tests for code examples
- Update SUMMARY.md if adding new files
- Request review from documentation maintainers

## Tools and Resources

### Recommended Tools

- **VS Code** with Markdown extensions
- **GitBook CLI** for local development
- **Markdown lint** for formatting checks
- **Link checker** for validation

### Useful Extensions

- **Markdown All in One**
- **Markdown Preview Enhanced**
- **GitBook Editor** (if using GitBook.com)

## Support

For documentation questions or issues:

- **GitHub Issues**: Use the `documentation` label
- **GitHub Discussions**: General documentation questions
- **Pull Requests**: Direct contributions and improvements

Remember: Good documentation is as important as good code. Take the time to write clear, accurate, and helpful documentation that will benefit all users of hstpd. 