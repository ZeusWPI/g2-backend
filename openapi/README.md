# Open API - Specification

This folder contains the Open API specification for the G2 project. Both backend & frontend will follow this spec.

## Viewing

You can view the spec using an Open API GUI tool (or just read the YAML files). You can use a tool like [StopLight](https://stoplight.io).
Message @maartenvn on [Mattermost](https://mattermost.zeus.gent) to receive access to the existing G2 workspace.

## Mocking

In order to speed up development of the frontend, independant of the backend, mocking can be used to mock the Open API specification.
This will generate fake data, according to the specification.

You can use a tool like [Prism](https://stoplight.io/open-source/prism/)

### Using Prism

You can use Prism in a few simple steps:
1. Make sure you have [Node.JS >= 12](https://nodejs.org) and [NPM](https://npmjs.com) installed.
2. Install Prism globally:

```bash
npm install -g @stoplight/prism-cli

# OR

yarn global add @stoplight/prism-cli
```

3. Run Prism from inside this folder:

```bash
prism mock reference/Default.v1.yaml -d
```
- **-d**: Dynamic mode. Will allow any value (of the correct type) for every variable.
