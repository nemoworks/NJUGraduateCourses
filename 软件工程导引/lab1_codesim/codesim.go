package main

import (
	"fmt"
	"log"
	"os"

	"github.com/Benjamin15122/simcalc"
	"github.com/urfave/cli"
)

func main() {
	//modify the help template
	cli.AppHelpTemplate = `NAME:
	{{.Name}} - {{.Description}}
 USAGE:
	{{.Usage}}
	{{if len .Authors}}
 AUTHOR:
	{{range .Authors}}{{ . }}{{end}}
	{{end}}{{if .Commands}}
 COMMANDS:
 {{range .Commands}}{{if not .HideHelp}}   {{join .Names ", "}}{{ "\t"}}{{.Usage}}{{ "\n" }}{{end}}{{end}}{{end}}{{if .VisibleFlags}}
 GLOBAL OPTIONS:
	{{range .VisibleFlags}}{{.}}
	{{end}}{{end}}{{if .Copyright }}
 COPYRIGHT:
	{{.Copyright}}
	{{end}}{{if .Version}}
 VERSION:
	{{.Version}}
	{{end}}
 `
	//define an app
	app := cli.NewApp()
	app.Description = "a simple tool for similarity check"
	app.Usage = "codesim [-d|--debug] [-h|--help] code1 code2\n	code1, code2: path for each file\n	output: a float64 ranging between[0-100], -1 if any runtime error"
	app.Author = "Guochang Wang"
	app.Version = "0.0.1"
	app.Flags = []cli.Flag{
		cli.BoolFlag{
			Name:  "debug, d",
			Usage: "print more info than just result",
		},
	}
	app.Action = func(c *cli.Context) error {
		if c.NArg() > 0 && c.NArg() != 2 {
			fmt.Println("two fileName are necessary")
			return nil
		}
		code1 := c.Args().Get(0)
		code2 := c.Args().Get(1)
		if c.Bool("debug") == true {
			simcalc.DebugCalculate(code1, code2)
		} else {
			simcalc.SimCalculate(code1, code2)
		}
		return nil
	}
	err := app.Run(os.Args)
	if err != nil {
		log.Fatal(err)
	}
}
