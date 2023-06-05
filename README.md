Self Assessment Individual Details API
========================

## Requirements

- Scala 2.13.x
- Java 11
- sbt 1.7.x
- [Service Manager V2](https://github.com/hmrc/sm2)

## Development Setup

Run from the console using: `sbt run` (starts on port NNNN by default)

Start the service manager profile: `sm2 --start MTDFB_SA`

## Running tests

```
sbt test
sbt it:test
```

## Viewing OAS

To view documentation locally ensure the Self Assessment Individual Details API is running, and run api-documentation-frontend:
`./run_local_with_dependencies.sh`

Then go to http://localhost:NNNN/api-documentation/docs/openapi/preview and use this port and version:
`http://localhost:NNNN/api/conf/1.0/application.yaml`

## Changelog

You can see our changelog [here](https://github.com/hmrc/income-tax-mtd-changelog)

## Support and Reporting Issues

You can create a GitHub issue [here](https://github.com/hmrc/income-tax-mtd-changelog/issues)

## API Reference / Documentation

Available on
the [Self Assessment Individual Details Documentation](https://developer.service.hmrc.gov.uk/api-documentation/docs/api/service/self-assessment-individual-details-api)

## License

This code is open source software licensed under
the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
