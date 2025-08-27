#!/bin/bash

# Test Coverage Script for E-Empuzitsi LMS
# Usage: ./run-coverage.sh [option]

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== E-Empuzitsi LMS Test Coverage Tool ===${NC}"
echo ""

show_help() {
    echo "Usage: $0 [OPTION]"
    echo ""
    echo "Options:"
    echo "  basic           Run tests with basic coverage report"
    echo "  check           Run tests with coverage verification (fails if < 70%)"
    echo "  entity          Run only entity tests with coverage"
    echo "  full            Run all tests with detailed coverage"
    echo "  report          Generate HTML coverage report and open it"
    echo "  clean           Clean previous coverage data"
    echo "  help            Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 basic        # Quick coverage check"
    echo "  $0 check        # Coverage with quality gates"
    echo "  $0 entity       # Entity tests only"
    echo "  $0 report       # Generate and open HTML report"
    echo ""
}

clean_coverage() {
    echo -e "${YELLOW}Cleaning previous coverage data...${NC}"
    ./mvnw clean -q
    rm -rf target/site/jacoco/
    echo -e "${GREEN}Coverage data cleaned.${NC}"
}

run_basic_coverage() {
    echo -e "${YELLOW}Running tests with basic coverage...${NC}"
    ./mvnw clean test jacoco:report -q
    
    if [ -f "target/site/jacoco/index.html" ]; then
        echo -e "${GREEN}✅ Coverage report generated successfully!${NC}"
        echo -e "${BLUE}📊 Coverage report: target/site/jacoco/index.html${NC}"
        
        # Extract coverage percentage from the report
        if command -v grep >/dev/null 2>&1; then
            coverage=$(grep -o 'Total[^%]*%' target/site/jacoco/index.html | head -1 | grep -o '[0-9]\+%' || echo "N/A")
            echo -e "${BLUE}📈 Overall Coverage: ${coverage}${NC}"
        fi
    else
        echo -e "${RED}❌ Failed to generate coverage report${NC}"
        exit 1
    fi
}

run_coverage_check() {
    echo -e "${YELLOW}Running tests with coverage verification...${NC}"
    
    if ./mvnw clean test jacoco:report jacoco:check -q; then
        echo -e "${GREEN}✅ All tests passed and coverage goals met!${NC}"
        
        if [ -f "target/site/jacoco/index.html" ]; then
            echo -e "${BLUE}📊 Coverage report: target/site/jacoco/index.html${NC}"
            
            # Extract coverage percentage
            if command -v grep >/dev/null 2>&1; then
                coverage=$(grep -o 'Total[^%]*%' target/site/jacoco/index.html | head -1 | grep -o '[0-9]\+%' || echo "N/A")
                echo -e "${BLUE}📈 Overall Coverage: ${coverage}${NC}"
            fi
        fi
    else
        echo -e "${RED}❌ Tests failed or coverage goals not met${NC}"
        echo -e "${YELLOW}💡 Tip: Check the coverage report for details${NC}"
        exit 1
    fi
}

run_entity_tests() {
    echo -e "${YELLOW}Running entity tests only with coverage...${NC}"
    ./mvnw clean test jacoco:report -Dtest="**/*EntityTest" -q
    
    if [ -f "target/site/jacoco/index.html" ]; then
        echo -e "${GREEN}✅ Entity test coverage completed!${NC}"
        echo -e "${BLUE}📊 Coverage report: target/site/jacoco/index.html${NC}"
    else
        echo -e "${RED}❌ Failed to generate entity coverage report${NC}"
        exit 1
    fi
}

run_full_coverage() {
    echo -e "${YELLOW}Running comprehensive test coverage...${NC}"
    ./mvnw clean verify jacoco:report -P coverage -q
    
    if [ -f "target/site/jacoco/index.html" ]; then
        echo -e "${GREEN}✅ Full coverage analysis completed!${NC}"
        echo -e "${BLUE}📊 Coverage report: target/site/jacoco/index.html${NC}"
        
        # Show detailed coverage info
        if [ -f "target/site/jacoco/jacoco.csv" ]; then
            echo -e "${BLUE}📋 Coverage Summary:${NC}"
            awk -F, 'NR>1 {instructions+=$4+$5; covered_instructions+=$4; branches+=$6+$7; covered_branches+=$6} 
                     END {
                         if (instructions > 0) {
                             inst_coverage = (covered_instructions/instructions)*100;
                             printf "   Instructions: %.1f%% (%d/%d)\n", inst_coverage, covered_instructions, instructions;
                         }
                         if (branches > 0) {
                             branch_coverage = (covered_branches/branches)*100;
                             printf "   Branches: %.1f%% (%d/%d)\n", branch_coverage, covered_branches, branches;
                         }
                     }' target/site/jacoco/jacoco.csv 2>/dev/null || echo "   Detailed metrics unavailable"
        fi
    else
        echo -e "${RED}❌ Failed to generate full coverage report${NC}"
        exit 1
    fi
}

open_coverage_report() {
    if [ -f "target/site/jacoco/index.html" ]; then
        echo -e "${BLUE}Opening coverage report...${NC}"
        
        # Try different ways to open the HTML file
        if command -v open >/dev/null 2>&1; then
            # macOS
            open target/site/jacoco/index.html
        elif command -v xdg-open >/dev/null 2>&1; then
            # Linux
            xdg-open target/site/jacoco/index.html
        elif command -v start >/dev/null 2>&1; then
            # Windows
            start target/site/jacoco/index.html
        else
            echo -e "${YELLOW}Cannot auto-open browser. Please open: target/site/jacoco/index.html${NC}"
        fi
        
        echo -e "${GREEN}✅ Coverage report opened in browser${NC}"
    else
        echo -e "${RED}❌ No coverage report found. Run coverage first.${NC}"
        echo -e "${YELLOW}💡 Try: $0 basic${NC}"
        exit 1
    fi
}

# Main script logic
case "${1:-help}" in
    "basic")
        run_basic_coverage
        ;;
    "check")
        run_coverage_check
        ;;
    "entity")
        run_entity_tests
        ;;
    "full")
        run_full_coverage
        ;;
    "report")
        open_coverage_report
        ;;
    "clean")
        clean_coverage
        ;;
    "help"|*)
        show_help
        ;;
esac

echo ""
echo -e "${BLUE}=== Coverage Analysis Complete ===${NC}"
