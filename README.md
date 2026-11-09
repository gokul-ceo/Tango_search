# TangoSearch

**Lightweight code search for AI coding agents.**

TangoSearch is an experimental code retrieval engine designed to help AI coding agents find relevant source code without overwhelming their context windows.

The project explores how traditional information retrieval techniques can be applied to code search, with a focus on relevance, efficiency, and keeping retrieved context small.

## Current Focus

- Exploring trigram-based indexing for code search.
- Comparing indexed search with traditional full-text scanning.
- Experimenting with retrieval performance on synthetic datasets.
- Investigating how to retrieve relevant code within limited context budgets.

## Motivation

AI agents can access large codebases, but providing more code doesn't necessarily produce better results.

The goal is simple: **retrieve the right code, not more code.**

## Status

🚧 Early experimental stage. Search algorithms, indexing strategies, and retrieval quality are still being explored.

## Roadmap

- [ ] Improve indexing and search performance.
- [ ] Experiment with real-world repositories.
- [ ] Improve result relevance and ranking.
- [ ] Explore token-aware retrieval.
- [ ] Integrate with AI coding agents through MCP.

## License

To be decided.
