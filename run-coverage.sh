#!/bin/bash

# Test Coverage Script for e-empuzitsi LMS
# Usage: ./run-coverage.sh [basic|check|entity|report]

set -e

echo "🧪 E-Empuzitsi LMS Test Coverage Runner"
echo "======================================"

# Function to run basic coverage
run_basic() {
    echo "📊 Running basic coverage analysis..."
    ./mvnw clean test
    echo "✅ Basic coverage completed!"
    echo "📁 Report location: target/site/jacoco/index.html"
}

# Function to run coverage with quality gates
run_check() {
    echo "🎯 Running coverage with quality gates..."
    ./mvnw clean test jacoco:check
    if [ $? -eq 0 ]; then
        echo "✅ Coverage quality gates passed!"
        echo "🎉 Project meets coverage standards (90% instruction, 85% branch)"
    else
        echo "❌ Coverage quality gates failed!"
        echo "💡 Check target/site/jacoco/index.html for details"
        exit 1
    fi
}

# Function to run entity tests only
run_entity() {
    echo "🗃️  Running entity tests with coverage..."
    ./mvnw clean test -Dtest="*EntityTest" jacoco:report
    echo "✅ Entity tests coverage completed!"
    echo "📁 Report location: target/site/jacoco/index.html"
}

# Function to open HTML report
open_report() {
    if [ -f "target/site/jacoco/index.html" ]; then
        echo "🌐 Opening coverage report in browser..."
        if command -v open >/dev/null 2>&1; then
            open target/site/jacoco/index.html
        elif command -v xdg-open >/dev/null 2>&1; then
            xdg-open target/site/jacoco/index.html
        else
            echo "📁 Please open target/site/jacoco/index.html manually"
        fi
    else
        echo "❌ No coverage report found. Run coverage first!"
        exit 1
    fi
}

# Function to show coverage summary
show_summary() {
    echo ""
    echo "📋 Coverage Summary:"
    echo "==================="
    if [ -f "target/site/jacoco/index.html" ]; then
        if command -v grep >/dev/null 2>&1; then
            # Extract coverage percentages from HTML report
            echo "📊 Coverage metrics will be displayed after opening the report"
        fi
    else
        echo "❌ No coverage report found. Run coverage first!"
    fi
}

# Main execution logic
case "${1:-basic}" in
    basic)
        run_basic
        show_summary
        ;;
    check)
        run_check
        show_summary
        ;;
    entity)
        run_entity
        show_summary
        ;;
    report)
        open_report
        ;;
    *)
        echo "❌ Unknown option: $1"
        echo ""
        echo "Usage: $0 [basic|check|entity|report]"
        echo ""
        echo "Options:"
        echo "  basic   - Run all tests and generate coverage report"
        echo "  check   - Run tests with quality gates (90% instruction, 85% branch)"
        echo "  entity  - Run entity tests only with coverage"
        echo "  report  - Open HTML coverage report in browser"
        echo ""
        exit 1
        ;;
esac

echo ""
echo "🎓 E-Empuzitsi LMS Test Coverage Complete!"
